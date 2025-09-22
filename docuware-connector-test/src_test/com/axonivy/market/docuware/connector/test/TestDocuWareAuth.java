package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.oauth.Configuration;
import com.axonivy.connector.docuware.connector.oauth.DocuWareAuthFeature;
import com.axonivy.connector.docuware.connector.oauth.Token;

import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest(enableWebServer = true)
public class TestDocuWareAuth {

	protected BearerDisableFilter bearerDisableFilter = new BearerDisableFilter();

	@BeforeEach
	public void prepareConfigurations(AppFixture fix) {
		fix.var("docuwareConnector.passwordtest.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.passwordtest.grantType", "password");
		fix.var("docuwareConnector.passwordtest.username", "testuser");
		fix.var("docuwareConnector.passwordtest.password", "testpassword");

		fix.var("docuwareConnector.trustedtest.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.trustedtest.grantType", "trusted");
		fix.var("docuwareConnector.trustedtest.username", "testuser");
		fix.var("docuwareConnector.trustedtest.password", "testpassword");
		fix.var("docuwareConnector.trustedtest.impersonateUser", "^ivy:system=admin,anonymous=admin");
	}

	@Test
	public void testGrantTypePassword(AppFixture fix) {
		var cfg = Configuration.getKnownConfigurationOrDefault("passwordtest");
		assertThat(cfg.tokenCacheKey()).endsWith("passwordtest:PASSWORD:testuser");

		// No token at the beginning.
		var cachedToken = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());
		assertThat(cachedToken).isNull();

		var rsp = getClient("passwordtest").path("Organizations").request(MediaType.APPLICATION_XML).get();
		assertThat(rsp.getStatusInfo().getFamily()).isEqualTo(Family.SUCCESSFUL);

		// Now there is a token (the mock service returns special non-JWT tokens to better distinguish).
		cachedToken = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());
		assertThat(token(cachedToken)).isNotNull().startsWith("password:testuser");
		assertThat(cachedToken.getConfig()).isEqualTo("passwordtest");

		Ivy.log().warn("token: {0}", cachedToken);

		rsp = getClient("passwordtest").path("Organizations").request(MediaType.APPLICATION_XML).get();

		// Now there is a cached token and it must be the same.
		var cachedToken2 = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());
		assertThat(token(cachedToken)).isEqualTo(token(cachedToken2));
	}

	@Test
	public void testGrantTypeTrusted(AppFixture fix) {
		var cfg = Configuration.getKnownConfigurationOrDefault("trustedtest");
		assertThat(cfg.tokenCacheKey()).endsWith("trustedtest:TRUSTED:admin");

		// No token at the beginning.
		var cachedToken = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());
		assertThat(cachedToken).isNull();

		var rsp = getClient("trustedtest").path("Organizations").request(MediaType.APPLICATION_XML).get();
		assertThat(rsp.getStatusInfo().getFamily()).isEqualTo(Family.SUCCESSFUL);

		// Now there is a token (the mock service returns special non-JWT tokens to better distinguish).
		cachedToken = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());
		assertThat(token(cachedToken)).isNotNull().startsWith("trusted:testuser:admin");
		assertThat(cachedToken.getConfig()).isEqualTo("trustedtest");

		Ivy.log().warn("token: {0}", cachedToken);

		rsp = getClient("trustedtest").path("Organizations").request(MediaType.APPLICATION_XML).get();

		// Now there is a cached token and it must be the same.
		var cachedToken2 = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());
		assertThat(token(cachedToken)).isEqualTo(token(cachedToken2));

		// Change user which should give us a new token.
		fix.loginUser("testuser1");
		rsp = getClient("trustedtest").path("Organizations").request(MediaType.APPLICATION_XML).get();

		var cachedToken3 = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());
		assertThat(token(cachedToken3)).isNotNull().startsWith("trusted:testuser:testuser1");
		assertThat(cachedToken3.getConfig()).isEqualTo("trustedtest");
		assertThat(token(cachedToken)).isNotEqualTo(token(cachedToken3));
	}

	protected String token(Token token) {
		String result = "";

		if(token != null) {
			if(token.getValues() != null) {
				result = (String)token.getValues().get("access_token");
			}
		}

		return result;
	}

	/**
	 * Make the Bearer token unreadable by Ivy, otherwise it will throw an error because it is not really a bearer token.
	 */
	protected class BearerDisableFilter implements ClientRequestFilter {

		@Override
		public void filter(ClientRequestContext requestContext) throws IOException {
			var headers = requestContext.getHeaders();
			var authHeader = headers.get(DocuWareAuthFeature.AUTHORIZATION).toString();
			if(authHeader != null && authHeader.contains(DocuWareAuthFeature.BEARER)) {
				headers.putSingle(DocuWareAuthFeature.AUTHORIZATION, authHeader.replace(DocuWareAuthFeature.BEARER, "IgnoredBearer "));
			}
		}
	}

	/**
	 * Get the client for a configuration.
	 * 
	 * @param configKey
	 * @return
	 */
	protected WebTarget getClient(String configKey) {
		return Ivy.rest().client(DocuWareService.CLIENT_NAME)
				.property(DocuWareService.CONFIG_KEY_PROPERTY, Configuration.knownOrDefaultKey(configKey))
				.register(bearerDisableFilter, Priorities.AUTHORIZATION + 1);
	}
}
