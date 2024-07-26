package com.axonivy.connector.docuware.connector;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ivyteam.ivy.data.cache.IDataCacheEntry;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.rest.client.FeatureConfig;
import ch.ivyteam.ivy.rest.client.RestClientFactoryConstants;

/**
 * Feature to make sure, that we have the DocuWare cookies.
 *
 * If they are not there, make a login request.
 */
public class DocuWareAuthFeature implements Feature, ClientRequestFilter, ClientResponseFilter {

  private static final String ACCOUNT_LOGON_PATH = "Account/Logon";

  public interface Property {

    String USERNAME = "UserName";
    String PASSWORD = "Password";
    String HOSTID = "HostId";
    String LOGONURL = "LogonUrl";
  }

  @Override
  public boolean configure(FeatureContext context) {
    Ivy.log().debug("DocuWare feature configure.");
    invalidateCookiesCacheEntry();
    context.register(this, Priorities.AUTHORIZATION);
    return true;
  }

  @Override
  public void filter(ClientRequestContext reqContext) throws IOException {
    if (isLogonRequest(reqContext.getUri())) {
      Ivy.log().info("DocuWare logon request.");
      return;
    }
    if (getCookies() == null) {
      Ivy.log().info("DocuWare logon, fetching cookies.");
      Configuration configuration = reqContext.getConfiguration();
      FeatureConfig config = new FeatureConfig(configuration, DocuWareAuthFeature.class);
      Form form = new Form().param("UserName", config.readMandatory(Property.USERNAME))
        .param("Password", config.readMandatory(Property.PASSWORD))
        .param("HostID", config.readMandatory(Property.HOSTID))
        .param("RedirectToMyselfInCaseOfError", "false");
      String logonUrl = getLogonUrl(reqContext, config);
      if (logonUrl == null) {
      }
      Ivy.log().info("DocuWare logon via URL {0}", logonUrl);
      Response response = logon(reqContext, form, logonUrl);
      Ivy.log().info("DocuWare logon returned with status {0}", response.getStatusInfo());
      if (getCookies() == null) {
        Ivy.log().error("DocuWare logon, still missing DocuWare cookies for context {0} after logon.", reqContext);
      }
    }
    requestHook(reqContext);
  }

  private String getLogonUrl(ClientRequestContext reqContext, FeatureConfig config) {
    String logonUrl = config.read(Property.LOGONURL).orElse(null);
    if (StringUtils.isNotBlank(logonUrl) && !logonUrl.trim().equalsIgnoreCase("AUTO")) {
      return logonUrl;
    }
    String host = reqContext.getUri().getHost();
    if (StringUtils.isBlank(host)) {
      throw new IllegalStateException("The variable 'docuware-connector.host' is missing or undefined!");
    }
    try {
      UUID clientId = UUID.fromString(config.readMandatory(RestClientFactoryConstants.PROPERTY_CLIENT_ID));
      var logon = Ivy.rest().client(clientId).resolveTemplate("host", host).path(ACCOUNT_LOGON_PATH).getUri();
      return logon.toString();
    } catch (Throwable t) {
      String message = String.format(
              "Could not determine DocuWare target URL automatically, please set it in REST client property '%s'. Put there the same URL as used for the client.",
              Property.LOGONURL);
      throw new IllegalStateException(message, t);
    }
  }

  protected Response logon(ClientRequestContext reqContext, Form form, String target) {
    Response response = reqContext.getClient().target(target)
            .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
            .accept(MediaType.APPLICATION_JSON).post(Entity.form(form));
    return response;
  }

  /**
   * Manipulate request if needed (probably only useful for tests).
   *
   * @param reqContext
   */
  protected void requestHook(@SuppressWarnings("unused") ClientRequestContext reqContext) {}

  @Override
  public void filter(ClientRequestContext reqContext, ClientResponseContext rspContext) throws IOException {
    if (!isLogonRequest(reqContext.getUri())) {
      return;
    }
    int status = rspContext.getStatus();
    if (rspContext.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
      String message = IOUtils.toString(rspContext.getEntityStream(), StandardCharsets.UTF_8);
      Ivy.log().error("DocuWare logon unsuccessful, URI: {0} Status: {1}, message:{2}", reqContext.getUri(), status,
          message);
      return;
    }
    Ivy.log().info("DocuWare logon successful: Status: {0}", status);
    DocuWareCookies docuWareCookies = DocuWareCookies.create(rspContext.getCookies());
    if (docuWareCookies.isValid()) {
      setCookiesCacheEntry(docuWareCookies);
    } else {
      Ivy.log().error(
              "DocuWare logon unsuccessful, did not receive the required cookies: {0} (URI: {1} Status: {2})",
              docuWareCookies, reqContext.getUri(), status);
    }
  }

  /**
   * Get the DocuWare cookies from the session.
   */
  public DocuWareCookies getCookies() {
    DocuWareCookies docuWareCookies = null;
    try {
      IDataCacheEntry entry = getCookiesCacheEntry();
      if (entry == null || !entry.isValid()) {
        Ivy.log().info("Got expired DocuWare cookies.");
      } else {
        docuWareCookies = (DocuWareCookies) entry.getValue();
      }
    } catch (Exception e) {
      Ivy.log().error("Exception while getting cached DocuWare cookies.", e);
    }
    return docuWareCookies;
  }

  /**
   * Get the cache entry of the DocuWare cookies.
   */
  public IDataCacheEntry getCookiesCacheEntry() {
    return Ivy.datacache().getAppCache().getEntry(DocuWareCookies.DW_COOKIES_CACHE_GROUP_ID,
            DocuWareCookies.DW_COOKIES_CACHE_ID);
  }

  /**
   * Cache the DocuWare cookies.
   *
   * @param docuWareCookies
   */
  public void setCookiesCacheEntry(DocuWareCookies docuWareCookies) {
    Ivy.log().info("Caching new DocuWare cookies: {0}", docuWareCookies);
    Ivy.datacache().getAppCache().setEntry(DocuWareCookies.DW_COOKIES_CACHE_GROUP_ID,
            DocuWareCookies.DW_COOKIES_CACHE_ID, DocuWareCookies.DW_COOKIES_EXPIRY_SECONDS, docuWareCookies);
  }

  /**
   * Invalidate the cached DocuWare cookies.
   */
  public void invalidateCookiesCacheEntry() {
    IDataCacheEntry entry = getCookiesCacheEntry();
    if (entry != null) {
      Ivy.log().info("Invalidating DocuWare cookies: {0}", entry.getValue());
      entry.invalidate();
    }
  }

  /**
   * Is this a logon request?
   *
   * @param uri
   */
  private static boolean isLogonRequest(URI uri) {
    return uri != null && uri.toString().endsWith(ACCOUNT_LOGON_PATH);
  }

  /**
   * Container for the cookies from DocuWare.
   *
   * The DocuWare Cookies have a very long life-time (some days), but here we go
   * down to one day. It will be longer than the Ivy session.
   */
  public static class DocuWareCookies {

    public static final String DW_COOKIES_CACHE_ID = "cookies";
    public static final String DW_COOKIES_CACHE_GROUP_ID = "com.docuware.cookies";
    public static final String DW_PLATFORM_AUTH_COOKIE = ".DWPLATFORMAUTH";
    public static final String DW_PLATFORM_BROWSER_ID = "DWPLATFORMBROWSERID";
    public static final int DW_COOKIES_EXPIRY_SECONDS = 86400;
    private NewCookie auth;
    private NewCookie browserId;

    private DocuWareCookies(Map<String, NewCookie> cookies) {
      if (Ivy.log().isDebugEnabled()) {
        for (Entry<String, NewCookie> entry : cookies.entrySet()) {
          NewCookie cookie = entry.getValue();
          Ivy.log().debug("Received cookie: {0}: Name: {1} Expiry: {2} Max Age: {3}", entry.getKey(),
                  cookie.getName(), cookie.getExpiry(), cookie.getMaxAge());
        }
      }
      auth = cookies.get(DocuWareCookies.DW_PLATFORM_AUTH_COOKIE);
      browserId = cookies.get(DocuWareCookies.DW_PLATFORM_BROWSER_ID);
    }

    /**
     * Create a new {@link DocuWareCookies} from the cookies received in the
     * REST client response.
     *
     * @param cookies
     */
    public static DocuWareCookies create(Map<String, NewCookie> cookies) {
      DocuWareCookies docuWareCookies = new DocuWareCookies(cookies);
      return docuWareCookies;
    }

    /**
     * Get the browser id cookie.
     */
    public NewCookie getBrowserId() {
      return browserId;
    }

    /**
     * Get the auth cookie.
     */
    public NewCookie getAuth() {
      return auth;
    }

    /**
     * Do we have the expected cookies?
     */
    public boolean isValid() {
      return auth != null && browserId != null;
    }

    @Override
    public String toString() {
      return String.format("DocuWareCookies [auth=%s, browserId=%s]", auth, browserId);
    }
  }
}
