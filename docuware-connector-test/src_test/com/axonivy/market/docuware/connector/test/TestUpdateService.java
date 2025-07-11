package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.market.docuware.connector.UpdateServiceData;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.scripting.objects.List;
import ch.ivyteam.ivy.security.ISession;

@IvyProcessTest(enableWebServer = true)
public class TestUpdateService extends TestDocuWareConnector {

  private static final int DOCUMENT_ID = 1;
  private static final BpmElement testeeUpdate = BpmProcess.path("UpdateService").elementName("updateDocument");
  private static final BpmElement testeeUpdate_2 = BpmProcess.path("UpdateService")
      .elementName("updateDocument(Integer, List<DocuWareProperty>, DocuWareEndpointConfiguration)");

  @Test
  public void updateDocumentWithEndpointConfiguration(BpmClient bpmClient, ISession session, AppFixture fixture,
      IApplication app) throws IOException {
    prepareRestClient(app, fixture);
    List<DocuWareProperty> propertyList = prepareDocuWareProperties();
    DocuWareEndpointConfiguration configuration = prepareDocuWareEndpointConfiguration();
    ExecutionResult result = bpmClient.start().subProcess(testeeUpdate_2)
        .withParam("documentId", String.valueOf(DOCUMENT_ID)).withParam("indexFields", propertyList)
        .withParam("configuration", configuration).execute();
    UpdateServiceData data = result.data().last();
    assertThat(data.getDocumentIndexFields()).isNotNull();
    assertThat(data.getDocumentIndexFields().getField()).isNotEmpty();
  }
}
