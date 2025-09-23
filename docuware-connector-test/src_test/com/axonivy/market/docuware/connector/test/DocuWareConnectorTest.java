package com.axonivy.market.docuware.connector.test;

import java.io.IOException;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.axonivy.connector.docuware.connector.DocuWareFieldTableItem;
import com.axonivy.connector.docuware.connector.DocuWareKeywordsField;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.oauth.Configuration;
import com.axonivy.connector.docuware.connector.oauth.DocuWareAuthFeature;
import com.axonivy.market.docuware.connector.test.DocuWareConnectorTest.BearerDisableFilter.BearerDisableFeature;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.rest.client.security.CsrfHeaderFeature;
import ch.ivyteam.ivy.scripting.objects.List;

public class DocuWareConnectorTest {
	protected BearerDisableFilter bearerDisableFilter = new BearerDisableFilter();

	/**
	 * Prepare the existing Rest Client.
	 * 
	 * <ul>
	 * <li>Make the bearer header unrecognizable.</li>
	 * <li>Add CSRF feature because the mock service expects it.</li>
	 * </ul>
	 * 
	 * @param app
	 * @param fixture
	 */
	@BeforeAll
	public static void prepareRestClient(IApplication app, AppFixture fixture) {
		var restClient = RestClients.of(app).find(DocuWareService.CLIENT_ID);

		var builder = RestClient.create(restClient.name())
				.uuid(restClient.uniqueId())
				.uri(restClient.uri())
				.description(restClient.description())
				.properties(restClient.properties());

		for (var feature : restClient.features()) {
			builder.feature(feature.clazz());
		}
		builder.feature(BearerDisableFeature.class.getCanonicalName());
		builder.feature(CsrfHeaderFeature.class.getCanonicalName());
		restClient = builder.toRestClient();
		RestClients.of(app).set(restClient);
	}


	/**
	 * Get the client for a configuration.
	 * 
	 * @param configKey
	 * @return
	 */
	protected WebTarget getClient(String configKey) {
		// Note, the client should be prepared in advance to add the bearer disable feature.
		return Ivy.rest().client(DocuWareService.CLIENT_ID)
				.property(DocuWareService.CONFIG_KEY_PROPERTY, Configuration.knownOrDefaultKey(configKey));
	}


	@BeforeEach
	public void prepareConfigurations(AppFixture fix) {
		fix.var("docuwareConnector.test.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.test.grantType", "password");
		fix.var("docuwareConnector.test.username", "testuser");
		fix.var("docuwareConnector.test.password", "testpassword");

		fix.var("docuwareConnector.passwordtest.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.passwordtest.grantType", "password");
		fix.var("docuwareConnector.passwordtest.username", "testuser");
		fix.var("docuwareConnector.passwordtest.password", "testpassword");

		fix.var("docuwareConnector.trustedtest1.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.trustedtest1.grantType", "trusted");
		fix.var("docuwareConnector.trustedtest1.username", "testuser");
		fix.var("docuwareConnector.trustedtest1.password", "testpassword");
		fix.var("docuwareConnector.trustedtest1.impersonateUser", "^ivy:system=sysuser,anonymous=anonuser");

		fix.var("docuwareConnector.trustedtest2.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.trustedtest2.grantType", "trusted");
		fix.var("docuwareConnector.trustedtest2.username", "testuser");
		fix.var("docuwareConnector.trustedtest2.password", "testpassword");
		fix.var("docuwareConnector.trustedtest2.impersonateUser", "^session");

		fix.var("docuwareConnector.trustedtest3.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.trustedtest3.grantType", "trusted");
		fix.var("docuwareConnector.trustedtest3.username", "testuser");
		fix.var("docuwareConnector.trustedtest3.password", "testpassword");
		fix.var("docuwareConnector.trustedtest3.impersonateUser", "^session");

		fix.var("docuwareConnector.dwtokentest1.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.dwtokentest1.grantType", "dwtoken");
		fix.var("docuwareConnector.dwtokentest1.dwToken", "^session");

		fix.var("docuwareConnector.dwtokentest2.url", "%s/api/docuWareMock".formatted(Ivy.html().applicationHomeLink().toAbsoluteUri().toString()));
		fix.var("docuwareConnector.dwtokentest2.grantType", "dwtoken");
		fix.var("docuwareConnector.dwtokentest2.dwToken", "^session");
	}

	protected List<DocuWareProperty> prepareDocuWareProperties() {
		var propertyList = new List<DocuWareProperty>();
		DocuWareProperty dwp = new DocuWareProperty();
		dwp.setFieldName("ACCESS_LEVEL");
		dwp.setItem("3");
		dwp.setItemElementName("String");
		propertyList.add(dwp);

		DocuWareFieldTableItem dwt = new DocuWareFieldTableItem();
		dwt.createRow().addColumnValue("NOTES__CONTENT", "HR input profile.", "String").addColumnValue("NOTES__AUTHOR",
				"PTA", "String");
		DocuWareProperty dwtp = new DocuWareProperty("EMPLOYEE_NOTES", dwt, "Table");
		propertyList.add(dwtp);

		DocuWareKeywordsField keywordField = new DocuWareKeywordsField();
		keywordField.append("1st Keyword");
		keywordField.append("2nd Keyword");
		DocuWareProperty keywordProperty = new DocuWareProperty("TAGS", keywordField, "Keywords");
		propertyList.add(keywordProperty);

		return propertyList;
	}

	/**
	 * Make the Bearer token unreadable by Ivy, otherwise it will throw an error because it is not really a bearer token.
	 */
	protected static class BearerDisableFilter implements ClientRequestFilter {

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

}
