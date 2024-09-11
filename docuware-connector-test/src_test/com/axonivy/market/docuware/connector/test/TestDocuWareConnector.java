package com.axonivy.market.docuware.connector.test;

import com.axonivy.connector.docuware.connector.DocuWareAuthFeature;
import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareFieldTableItem;
import com.axonivy.connector.docuware.connector.DocuWareProperty;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClient.Builder;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.scripting.objects.List;

@IvyProcessTest(enableWebServer = true)
public class TestDocuWareConnector {

  protected void prepareRestClient(IApplication app, AppFixture fixture) {
    fixture.var("docuwareConnector.host", "TESTHOST");
    fixture.var("docuwareConnector.username", "TESTUSER");
    fixture.var("docuwareConnector.password", "TESTPASS");
    fixture.var("docuwareConnector.hostid", "TESTHOSTID");
    fixture.var("docuwareConnector.filecabinetid", "123");
    RestClient restClient = RestClients.of(app).find("DocuWare");
    // change created client: use test url and a slightly different version of
    // the
    // DocuWare Auth feature
    Builder builder = RestClient.create(restClient.name()).uuid(restClient.uniqueId())
            .uri("http://{ivy.engine.host}:{ivy.engine.http.port}/{ivy.request.application}/api/docuWareMock")
            .description(restClient.description()).properties(restClient.properties());
    // use test feature instead of real one
    for (String feature : restClient.features()) {
      if (feature.contains(DocuWareAuthFeature.class.getCanonicalName())) {
        feature = DocuWareAuthTestFeature.class.getCanonicalName();
      }
      builder.feature(feature);
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
    dwt.createRow()
      .addColumnValue("NOTES__CONTENT", "HR input profile.", "String")
      .addColumnValue("NOTES__AUTHOR", "PTA", "String")
    ;
    DocuWareProperty dwtp = new DocuWareProperty("EMPLOYEE_NOTES", dwt, "Table");
    propertyList.add(dwtp);
    
    return propertyList;
  }

  protected DocuWareEndpointConfiguration prepareDocuWareEndpointConfiguration() {
    DocuWareEndpointConfiguration configuration = new DocuWareEndpointConfiguration();
    return configuration;
  }
}
