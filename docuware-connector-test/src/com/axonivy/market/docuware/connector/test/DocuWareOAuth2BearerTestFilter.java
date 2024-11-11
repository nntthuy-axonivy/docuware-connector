package com.axonivy.market.docuware.connector.test;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;

import com.axonivy.connector.docuware.connector.auth.oauth.OAuth2BearerFilter;
import com.axonivy.connector.docuware.connector.auth.oauth.OAuth2TokenRequester;
import com.axonivy.connector.docuware.connector.auth.oauth.OAuth2UriProperty;

public class DocuWareOAuth2BearerTestFilter extends OAuth2BearerFilter {

  public DocuWareOAuth2BearerTestFilter(OAuth2TokenRequester getToken, OAuth2UriProperty uriFactory) {
    super(getToken, uriFactory);
  }

  @Override
  public void filter(ClientRequestContext context) throws IOException {
    context.getHeaders().add(AUTHORIZATION, BEARER + "mockAccessToken");
  }

}
