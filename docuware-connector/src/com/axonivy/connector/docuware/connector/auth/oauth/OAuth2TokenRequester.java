package com.axonivy.connector.docuware.connector.auth.oauth;

import java.util.Optional;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.axonivy.connector.docuware.connector.enums.GrantType;

import ch.ivyteam.ivy.rest.client.FeatureConfig;
import ch.ivyteam.ivy.rest.client.oauth2.uri.OAuth2UriProvider;

/**
 * Requests a OAuth2 bearer access token
 */
public interface OAuth2TokenRequester {
  /**
   * Requests a OAuth2 bearer access token
   * 
   * @param ctxt authentication context
   * @return response from the remote accessToken request. Yet with an unread
   *         entity.
   */
  Response requestToken(AuthContext ctxt);

  /**
   * The authentication context to be used to fire an 'accessToken' request.
   */
  public static class AuthContext {
    /**
     * JAX-RS client pre-configured to call the accessToken URI defined by
     * {@link OAuth2UriProvider#getTokenUri()}
     */
    public final WebTarget target;

    /**
     * Current feature configuration provided by Rest Client properties or
     * programmatic features.
     */
    public final FeatureConfig config;

    private final Optional<GrantType> grantType;

    AuthContext(WebTarget target, FeatureConfig config, GrantType grantType) {
      this.target = target;
      this.config = config;
      this.grantType = Optional.ofNullable(grantType);
    }

    public Optional<GrantType> grantType() {
      return grantType;
    }
  }
}