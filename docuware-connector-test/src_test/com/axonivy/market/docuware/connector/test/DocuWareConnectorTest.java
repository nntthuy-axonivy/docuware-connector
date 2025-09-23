package com.axonivy.market.docuware.connector.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.axonivy.connector.docuware.connector.DocuWareFieldTableItem;
import com.axonivy.connector.docuware.connector.DocuWareKeywordsField;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.market.docuware.connector.test.BearerDisableFilter.BearerDisableFeature;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.rest.client.security.CsrfHeaderFeature;
import ch.ivyteam.ivy.scripting.objects.List;

public class DocuWareConnectorTest {
	/**
	 * Prepare the existing Rest Client.
	 * 
	 * <ul>
	 * <li>Make the bearer header unrecognizable so that we can use the token to transport information in the tests.</li>
	 * <li>Add CSRF feature because Ivy requires it for the mock service.</li>
	 * </ul>
	 * 
	 * Other than that, the original client and features are used.
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
}
