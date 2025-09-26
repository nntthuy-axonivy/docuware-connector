package com.axonivy.market.docuware.connector.test;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.axonivy.connector.docuware.connector.DocuWareFieldTableItem;
import com.axonivy.connector.docuware.connector.DocuWareKeywordsField;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.auth.DocuWareAuthFeature;
import com.axonivy.market.docuware.connector.test.BearerDisableFilter.BearerDisableFeature;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.request.EngineUriResolver;
import ch.ivyteam.ivy.rest.client.mapper.JsonFeature;
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
		fixture.config("RestClients.DocuWare.Features", java.util.List.of(
				JsonFeature.class.getCanonicalName(),
				MultiPartFeature.class.getCanonicalName(),
				DocuWareAuthFeature.class.getCanonicalName(),
				BearerDisableFeature.class.getCanonicalName(),
				CsrfHeaderFeature.class.getName()));
	}

	@AfterAll
	public static void showMBeanExceptionNotice() {
		Ivy.log().warn("Note: Certain AxonIvy versions may log exceptions when registering the DocuWare REST Web Service MBean. These exceptions are harmless and can be ignored.");
	}

	@BeforeEach
	public void prepareConfigurations(AppFixture fix, IApplication app) {
		// The URL of the local DocuWare mock service.
		var docuWareMockUrl = "%s/%s/api/docuWareMock".formatted(EngineUriResolver.instance().local(), app.getContextPath());

		fix.var("docuwareConnector.test.url", docuWareMockUrl);
		fix.var("docuwareConnector.test.grantType", "password");
		fix.var("docuwareConnector.test.username", "testuser");
		fix.var("docuwareConnector.test.password", "testpassword");

		fix.var("docuwareConnector.passwordtest.url", docuWareMockUrl);
		fix.var("docuwareConnector.passwordtest.grantType", "password");
		fix.var("docuwareConnector.passwordtest.username", "testuser");
		fix.var("docuwareConnector.passwordtest.password", "testpassword");

		fix.var("docuwareConnector.trustedtest1.url", docuWareMockUrl);
		fix.var("docuwareConnector.trustedtest1.grantType", "trusted");
		fix.var("docuwareConnector.trustedtest1.username", "testuser");
		fix.var("docuwareConnector.trustedtest1.password", "testpassword");
		fix.var("docuwareConnector.trustedtest1.impersonateUser", "^ivy:system=sysuser,anonymous=anonuser");

		fix.var("docuwareConnector.trustedtest2.url", docuWareMockUrl);
		fix.var("docuwareConnector.trustedtest2.grantType", "trusted");
		fix.var("docuwareConnector.trustedtest2.username", "testuser");
		fix.var("docuwareConnector.trustedtest2.password", "testpassword");
		fix.var("docuwareConnector.trustedtest2.impersonateUser", "^session");

		fix.var("docuwareConnector.trustedtest3.url", docuWareMockUrl);
		fix.var("docuwareConnector.trustedtest3.grantType", "trusted");
		fix.var("docuwareConnector.trustedtest3.username", "testuser");
		fix.var("docuwareConnector.trustedtest3.password", "testpassword");
		fix.var("docuwareConnector.trustedtest3.impersonateUser", "^session");

		fix.var("docuwareConnector.dwtokentest1.url", docuWareMockUrl);
		fix.var("docuwareConnector.dwtokentest1.grantType", "dwtoken");
		fix.var("docuwareConnector.dwtokentest1.dwToken", "^session");

		fix.var("docuwareConnector.dwtokentest2.url", docuWareMockUrl);
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
		dwt.createRow()
		.addColumnValue("NOTES__CONTENT", "HR input profile.", "String")
		.addColumnValue("NOTES__AUTHOR", "PTA", "String");
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
