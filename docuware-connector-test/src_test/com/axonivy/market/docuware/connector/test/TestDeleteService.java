package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.market.docuware.connector.DeleteServiceData;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.security.ISession;

@IvyProcessTest(enableWebServer = true)
public class TestDeleteService extends TestDocuWareConnector {

  private static final int DOCUMENT_ID = 2;
  private static final BpmElement testeeDelete = BpmProcess.path("DeleteService").elementName("deleteDocument(String)");
  private static final BpmElement testeeDelete_2 = BpmProcess.path("DeleteService")
      .elementName("deleteDocument(String, DocuWareEndpointConfiguration)");

  @Test
  public void deleteDocumentWithEndpointConfiguration(BpmClient bpmClient, ISession session, AppFixture fixture,
      IApplication app) throws IOException {
    prepareRestClient(app, fixture);
    DocuWareEndpointConfiguration configuration = prepareDocuWareEndpointConfiguration();
    ExecutionResult result = bpmClient.start().subProcess(testeeDelete_2)
        .withParam("documentId", String.valueOf(DOCUMENT_ID)).withParam("configuration", configuration).execute();
    DeleteServiceData data = result.data().last();
    assertThat(data.getError()).isNull();
  }
}
