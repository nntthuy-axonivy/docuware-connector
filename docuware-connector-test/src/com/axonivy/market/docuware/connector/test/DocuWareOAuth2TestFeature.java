package com.axonivy.market.docuware.connector.test;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import com.axonivy.connector.docuware.connector.auth.OAuth2Feature;

/**
 * Currently, calling REST services directly in a {@link Feature} is not
 * supported in the Ivy Test environment. This feature overrides the real login
 * call and instead sets the needed cookies directly.
 */
public class DocuWareOAuth2TestFeature extends OAuth2Feature {

  /**
   * Do not perform a real request access token.
   */

  @Override
  public boolean configure(FeatureContext context) {
    var oauth2 = new DocuWareOAuth2BearerTestFilter(null, null);
    context.register(oauth2, Priorities.AUTHORIZATION);

    return true;
  }
}
