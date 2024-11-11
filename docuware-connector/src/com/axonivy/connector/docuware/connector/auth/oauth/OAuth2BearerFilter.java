
package com.axonivy.connector.docuware.connector.auth.oauth;

import static com.axonivy.connector.docuware.connector.utils.DocuWareUtils.getIvyVar;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response.Status.Family;

import com.axonivy.connector.docuware.connector.auth.oauth.OAuth2TokenRequester.AuthContext;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.axonivy.connector.docuware.connector.enums.GrantType;

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
    VarTokenStore accessTokenStore = VarTokenStore.get(DocuWareVariable.ACCESS_TOKEN.getVariableName());
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

  private static BpmPublicErrorBuilder authError() {
    return BpmError.create(RedirectToIdentityProvider.OAUTH2_ERROR_CODE);
  }
}
