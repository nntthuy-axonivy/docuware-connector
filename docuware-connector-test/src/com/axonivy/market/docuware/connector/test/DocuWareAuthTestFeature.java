package com.axonivy.market.docuware.connector.test;

import java.io.IOException;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.FeatureContext;

import com.axonivy.connector.docuware.connector.oauth.DocuWareAuthFeature;

public class DocuWareAuthTestFeature extends DocuWareAuthFeature {
	/**
	 * Do not perform a real request access token.
	 */

	@Override
	public boolean configure(FeatureContext context) {
		var bearerFilter = new DocuWareBearerTestFilter();
		context.register(bearerFilter, Priorities.AUTHORIZATION);
		return true;
	}

	public class DocuWareBearerTestFilter extends DocuWareBearerFilter {
		@Override
		public void filter(ClientRequestContext context) throws IOException {
			// Set the Authorization Header but no recognizable Bearer Token to avoid automatic parsing of Ivy.
			context.getHeaders().add(AUTHORIZATION, "TestBearer mockAccessToken");
		}
	}
}
