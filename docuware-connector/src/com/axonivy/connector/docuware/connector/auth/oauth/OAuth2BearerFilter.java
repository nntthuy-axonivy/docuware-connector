
package com.axonivy.connector.docuware.connector.auth.oauth;

import static com.axonivy.connector.docuware.connector.utils.DocuWareUtils.getIvyVar;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.auth.oauth.OAuth2TokenRequester.AuthContext;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.axonivy.connector.docuware.connector.enums.GrantType;
import com.axonivy.connector.docuware.connector.utils.DocuWareUtils;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.bpm.error.BpmPublicErrorBuilder;
import ch.ivyteam.ivy.rest.client.FeatureConfig;
import ch.ivyteam.ivy.rest.client.internal.oauth2.RedirectToIdentityProvider;

@SuppressWarnings("restriction")
public class OAuth2BearerFilter implements javax.ws.rs.client.ClientRequestFilter {
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER = "Bearer ";
  public static final String CODE_PARAM = "code";

  private final OAuth2TokenRequester getToken;
  private final OAuth2UriProperty uriFactory;

  public OAuth2BearerFilter(OAuth2TokenRequester getToken, OAuth2UriProperty uriFactory) {
    this.getToken = getToken;
    this.uriFactory = uriFactory;
  }

  @Override
  public void filter(ClientRequestContext context) throws IOException {
    if (uriFactory.isAuthRequest(context.getUri()) || context.getHeaders().containsKey(AUTHORIZATION)) {
      return;
    }

    String accessToken = getAccessToken(context);
    context.getHeaders().add(AUTHORIZATION, BEARER + accessToken);
  }

  private final String getAccessToken(ClientRequestContext context) {
    VarTokenStore accessTokenStore = VarTokenStore.get();
    var accessToken = accessTokenStore.getToken();

    if (accessToken == null || accessToken.isExpired()) {
      FeatureConfig config = new FeatureConfig(context.getConfiguration(), getSource());
      accessToken = getNewAccessToken(context.getClient(), config);
      accessTokenStore.setToken(accessToken);
    }

    if (!accessToken.hasAccessToken()) {
      accessTokenStore.setToken(null);
      authError().withMessage("Failed to read 'access_token' from " + accessToken).throwError();
    }

    return accessToken.accessToken();
  }

  private Class<?> getSource() {
    Class<?> type = getToken.getClass();
    Class<?> declaring = type.getDeclaringClass();
    if (declaring != null) {
      return declaring;
    }
    return type;
  }

  private Token getNewAccessToken(Client client, FeatureConfig config) {
    loadMandatoryConfigurationByActiveInstance();
    String type = getIvyVar(DocuWareVariable.GRANT_TYPE);
    GrantType grantType = Optional.ofNullable(GrantType.of(type)).orElse(GrantType.PASSWORD);

    GenericType<Map<String, Object>> map = new GenericType<>(Map.class);

    var tokenUri = uriFactory.getTokenUri();
    var authContext = new AuthContext(client.target(tokenUri), config, grantType);
    var response = getToken.requestToken(authContext);
    if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
      return new Token(response.readEntity(map));
    }
    throw authError().withMessage("Failed to get access token: " + response)
        .withAttribute("status", response.getStatus()).withAttribute("payload", response.readEntity(String.class))
        .build();
  }

  private void loadMandatoryConfigurationByActiveInstance() {
    DocuWareEndpointConfiguration defaultConfiguration = DocuWareUtils.getDefaultActiveInstance();
    if (defaultConfiguration == null) {
      DocuWareService.unifyConfigurationByInstance();
      defaultConfiguration = DocuWareUtils.getDefaultActiveInstance();
    }
    if (defaultConfiguration != null) {
      // Load configuration for Host
      String activeHost = DocuWareUtils.getIvyVar(DocuWareVariable.HOST);
      if (isNoneBlank(defaultConfiguration.getHost())
          && (isBlank(activeHost) || !activeHost.equals(defaultConfiguration.getHost()))) {
        DocuWareUtils.setIvyVar(DocuWareVariable.HOST, defaultConfiguration.getHost());
      }
      // Load configuration for connect timeout
      String activeConnectTimeoutString = DocuWareUtils.getIvyVar(DocuWareVariable.CONNECT_TIMEOUT);
      int activeConnectTimeout = NumberUtils.isCreatable(activeConnectTimeoutString) ? NumberUtils.toInt(activeConnectTimeoutString) : 0;
      if (defaultConfiguration.getConnectTimeout() != null
          && activeConnectTimeout != defaultConfiguration.getConnectTimeout()) {
        DocuWareUtils.setIvyVar(DocuWareVariable.CONNECT_TIMEOUT, activeConnectTimeoutString);
      }
    }
  }

  private static BpmPublicErrorBuilder authError() {
    return BpmError.create(RedirectToIdentityProvider.OAUTH2_ERROR_CODE);
  }
}
