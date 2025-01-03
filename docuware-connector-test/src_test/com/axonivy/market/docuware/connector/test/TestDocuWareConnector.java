package com.axonivy.market.docuware.connector.test;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareFieldTableItem;
import com.axonivy.connector.docuware.connector.DocuWareKeywordsField;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.auth.OAuth2Feature;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClient.Builder;
import ch.ivyteam.ivy.rest.client.RestClientFeature;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.scripting.objects.List;

@IvyProcessTest(enableWebServer = true)
public class TestDocuWareConnector {

  protected void prepareRestClient(IApplication app, AppFixture fixture) {
    fixture.var("docuwareConnector.host", "TESTHOST");
    fixture.var("docuwareConnector.username", "TESTUSER");
    fixture.var("docuwareConnector.password", "TESTPASS");
    fixture.var("docuwareConnector.filecabinetid", "123");
    RestClient restClient = RestClients.of(app).find("DocuWare");
    // change created client: use test url and a slightly different version of
    // the
    // DocuWareOAuth2TestFeature
    Builder builder = RestClient.create(restClient.name()).uuid(restClient.uniqueId())
        .uri("http://{ivy.engine.host}:{ivy.engine.http.port}/{ivy.request.application}/api/docuWareMock")
        .description(restClient.description()).properties(restClient.properties());
    // use test feature instead of real one
    for (RestClientFeature feature : restClient.features()) {
      if (feature.clazz().contains(OAuth2Feature.class.getCanonicalName())) {
        feature = new RestClientFeature(DocuWareOAuth2TestFeature.class.getCanonicalName());
      }
      builder.feature(feature.clazz());
    }
    builder.feature("ch.ivyteam.ivy.rest.client.security.CsrfHeaderFeature");
    restClient = builder.toRestClient();
    RestClients.of(app).set(restClient);
  }

  protected List<DocuWareProperty> prepareDocuWareProperties() {
    List<DocuWareProperty> propertyList = new List<DocuWareProperty>();
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

  protected DocuWareEndpointConfiguration prepareDocuWareEndpointConfiguration() {
    DocuWareEndpointConfiguration configuration = new DocuWareEndpointConfiguration();
    return configuration;
  }
}
