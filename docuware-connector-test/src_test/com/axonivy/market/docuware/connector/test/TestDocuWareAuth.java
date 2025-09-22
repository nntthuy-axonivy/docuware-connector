package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.oauth.Configuration;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.environment.IvyTest;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClients;

@IvyTest(enableWebServer = true)
public class TestDocuWareAuth {

	protected void prepareRestClient(IApplication app, AppFixture fixture) {
		var restClient = RestClients.of(app).find("DocuWare");
		// Change created client.
		var builder = RestClient.create(restClient.name()).uuid(restClient.uniqueId())
				.uri("http://{ivy.engine.host}:{ivy.engine.http.port}/{ivy.request.application}/api/docuWareMock")
				.description(restClient.description()).properties(restClient.properties());
		// use test feature instead of real one
		for (var feature : restClient.features()) {
			builder.feature(feature.clazz());
		}
		restClient = builder.toRestClient();
		RestClients.of(app).set(restClient);
	}

	/**
	 * Get the client for a configuration.
	 * 
	 * @param configKey
	 * @return
	 */
	public WebTarget getClient(String configKey) {
		return Ivy.rest().client(DocuWareService.CLIENT_NAME).property(DocuWareService.CONFIG_KEY_PROPERTY, Configuration.knownOrDefaultKey(configKey));
	}

	@Test
	public void testGrantTypePassword(AppFixture fix, IApplication app) {
		fix.var("docuwareConnector.test.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.test.grantType", "password");
		fix.var("docuwareConnector.test.username", "testuser");
		fix.var("docuwareConnector.test.password", "testpassword");
		var client = getClient("test");
		var rsp = client.path("Organizations").request(MediaType.APPLICATION_XML).get();

		var cfg = Configuration.getKnownConfigurationOrDefault("test");

		var cachedToken = DocuWareService.get().getCachedToken(cfg.tokenCacheKey());

		assertThat(rsp).isNotNull();
	}
}
