package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.auth.DocuWareAuthFeature;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest(enableWebServer = true)
public class DocuWareAuthFeatureTest extends DocuWareConnectorTest {

	@Test
	void testRequiredForConnecting(AppFixture fix) {
		fix.var("docuwareConnector.test.url", "");
		fix.var("docuwareConnector.test.grantType", "");

		assertThatExceptionOfType(ProcessingException.class).isThrownBy(() ->
		ClientBuilder.newClient().register(new DocuWareAuthFeature())
		.property(DocuWareService.CONFIG_KEY_PROPERTY, "test")
		.target("someResource").request().get())
		.extracting(Exception::getCause)
		.isInstanceOf(BpmError.class)
		.extracting(e -> ((BpmError)e).getErrorCode())
		.isEqualTo("docuware:connector:configuration:nourl");

		DocuWareService.get().clearCaches();

		fix.var("docuwareConnector.test.url", "nowhere");
		fix.var("docuwareConnector.test.grantType", "");

		assertThatExceptionOfType(ProcessingException.class).isThrownBy(() ->
		ClientBuilder.newClient().register(new DocuWareAuthFeature())
		.property(DocuWareService.CONFIG_KEY_PROPERTY, "test")
		.target("someResource").request().get())
		.extracting(Exception::getCause)
		.isInstanceOf(BpmError.class)
		.extracting(e -> ((BpmError)e).getErrorCode())
		.isEqualTo("docuware:connector:configuration:nogranttype");

		DocuWareService.get().clearCaches();

		fix.var("docuwareConnector.test.url", "nowhere");
		fix.var("docuwareConnector.test.grantType", "password");

		assertThatExceptionOfType(Exception.class).isThrownBy(() ->
		ClientBuilder.newClient().register(new DocuWareAuthFeature())
		.property(DocuWareService.CONFIG_KEY_PROPERTY, "test")
		.target("someResource").request().get())
		.withMessageContaining("URI is not absolute");
	}
}
