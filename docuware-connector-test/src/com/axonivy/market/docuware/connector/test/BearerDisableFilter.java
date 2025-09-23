package com.axonivy.market.docuware.connector.test;

import java.io.IOException;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import com.axonivy.connector.docuware.connector.oauth.DocuWareAuthFeature;

/**
 * Make the Bearer token unreadable by Ivy, otherwise it will throw an error because it is not really a bearer token.
 */
class BearerDisableFilter implements ClientRequestFilter {

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		var headers = requestContext.getHeaders();
		var authHeader = headers.get(DocuWareAuthFeature.AUTHORIZATION);

		if(authHeader != null) {
			var authValue = authHeader.toString();
			if(authValue.contains(DocuWareAuthFeature.BEARER)) {
				headers.putSingle(DocuWareAuthFeature.AUTHORIZATION, authValue.replace(DocuWareAuthFeature.BEARER, "IgnoredBearer "));
			}
		}
	}

	protected static class BearerDisableFeature implements Feature {
		@Override
		public boolean configure(FeatureContext context) {
			var bearerFilter = new BearerDisableFilter();
			context.register(bearerFilter, Priorities.AUTHORIZATION + 1);
			return true;
		}

	}
}