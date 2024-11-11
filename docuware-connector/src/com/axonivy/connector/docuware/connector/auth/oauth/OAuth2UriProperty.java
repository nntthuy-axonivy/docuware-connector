package com.axonivy.connector.docuware.connector.auth.oauth;

import java.net.URI;

import ch.ivyteam.ivy.rest.client.FeatureConfig;

public class OAuth2UriProperty extends ch.ivyteam.ivy.rest.client.oauth2.uri.OAuth2UriProperty {

  public static final String TOKEN_RELATIVE_PATH = "";

  public OAuth2UriProperty(FeatureConfig config, String defaultBaseUri) {
    super(config, defaultBaseUri);
  }

  @Override
  public URI getTokenUri() {
    return getUri(TOKEN_RELATIVE_PATH);
  }
}
