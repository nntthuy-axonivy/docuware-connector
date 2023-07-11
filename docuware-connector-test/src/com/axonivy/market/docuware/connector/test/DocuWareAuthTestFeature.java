package com.axonivy.market.docuware.connector.test;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.axonivy.connector.docuware.connector.DocuWareAuthFeature;

/**
 * Currently, calling REST services directly in a {@link Feature} is not
 * supported in the Ivy Test environment. This feature overrides the real login
 * call and instead sets the needed cookies directly.
 */
public class DocuWareAuthTestFeature extends DocuWareAuthFeature {

  /**
   * Do not perform a real logon, but only set the cookies.
   */
  @Override
  protected Response logon(ClientRequestContext reqContext, Form form, String target) {
    DocuWareCookies docuWareCookies = DocuWareCookies.create(Map.of(
      DocuWareCookies.DW_PLATFORM_AUTH_COOKIE,
      new NewCookie(DocuWareCookies.DW_PLATFORM_AUTH_COOKIE, "Auth Cookie"),
      DocuWareCookies.DW_PLATFORM_BROWSER_ID,
      new NewCookie(DocuWareCookies.DW_PLATFORM_BROWSER_ID, "Browser Id Cookie")
    ));
    setCookiesCacheEntry(docuWareCookies);
    return Response.ok().build();
  }

  /**
   * Set the cookies into the request, because they were not set by the result
   * of the logon request.
   */
  @Override
  protected void requestHook(ClientRequestContext reqContext) {
    super.requestHook(reqContext);
    DocuWareCookies docuWareCookies = getCookies();
    if (docuWareCookies != null && docuWareCookies.isValid()) {
      reqContext.getHeaders().put("Cookie", List.of(
        docuWareCookies.getAuth().toCookie(),
        docuWareCookies.getBrowserId().toCookie())
      );
    } else {
      throw new RuntimeException("Expected valud DocuWare Cookies but got: " + docuWareCookies);
    }
  }
}
