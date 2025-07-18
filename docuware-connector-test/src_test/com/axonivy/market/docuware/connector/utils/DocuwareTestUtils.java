package com.axonivy.market.docuware.connector.utils;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareFieldTableItem;
import com.axonivy.connector.docuware.connector.DocuWareKeywordsField;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.auth.OAuth2Feature;
import com.axonivy.market.docuware.connector.constants.DocuwareTestConstants;
import com.axonivy.market.docuware.connector.test.DocuWareOAuth2TestFeature;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClientFeature;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.rest.client.RestClient.Builder;
import ch.ivyteam.ivy.scripting.objects.List;

public class DocuwareTestUtils {

  public static void setUpConfigForContext(String contextName, AppFixture fixture, IApplication app) {
    switch (contextName) {
      case DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME:
        setUpConfigForApiTest(fixture);
        break;
      case DocuwareTestConstants.MOCK_SERVER_CONTEXT_DISPLAY_NAME:
        setUpConfigForMockServer(fixture, app);
        break;
      default:
        break;
    }
  }

  public static void setUpConfigForMockServer(AppFixture fixture, IApplication app) {
    fixture.var("docuwareConnector.host", "TESTHOST");
    fixture.var("docuwareConnector.username", "TESTUSER");
    fixture.var("docuwareConnector.password", "TESTPASS");
    fixture.var("docuwareConnector.filecabinetid", "123");
    RestClient restClient = RestClients.of(app).find("DocuWare");
    // change created client: use test url and a slightly different version of the DocuWareOAuth2TestFeature
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

  public static void setUpConfigForApiTest(AppFixture fixture) {
    String host = System.getProperty(DocuwareTestConstants.HOST);
    String defaultInstance = System.getProperty(DocuwareTestConstants.DEFAULT_INSTANCE);
    String instanceHost = System.getProperty(DocuwareTestConstants.INSTANCE_HOST);
    String instanceGrantType = System.getProperty(DocuwareTestConstants.INSTANCE_GRANT_TYPE);
    String instanceUsername = System.getProperty(DocuwareTestConstants.INSTANCE_USERNAME);
    String instancePassword = System.getProperty(DocuwareTestConstants.INSTANCE_PASSWORD);
    String trustedUserName = System.getProperty(DocuwareTestConstants.INSTANCE_TRUSTED_USERNAME);
    String trustedUserPassword = System.getProperty(DocuwareTestConstants.INSTANCE_TRUSTED_USER_PASSWORD);
    String fileCabinetid = System.getProperty(DocuwareTestConstants.FILE_CABINET_ID);

    fixture.var("docuwareConnector.host", host);
    fixture.var("docuwareConnector.defaultInstance", defaultInstance);
    fixture.var("docuwareConnector.instances.primary.host", instanceHost);
    fixture.var("docuwareConnector.instances.primary.grantType", instanceGrantType);
    fixture.var("docuwareConnector.instances.primary.username", instanceUsername);
    fixture.var("docuwareConnector.instances.primary.password", instancePassword);
    fixture.var("docuwareConnector.instances.primary.trustedUserName", trustedUserName);
    fixture.var("docuwareConnector.instances.primary.trustedUserPassword", trustedUserPassword);
    fixture.var("docuwareConnector.instances.primary.filecabinetid", fileCabinetid);
  }

  public static List<DocuWareProperty> prepareDocuWareProperties() {
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

  public static DocuWareEndpointConfiguration prepareDocuWareEndpointConfiguration() {
    DocuWareEndpointConfiguration configuration = new DocuWareEndpointConfiguration();
    return configuration;
  }
}
