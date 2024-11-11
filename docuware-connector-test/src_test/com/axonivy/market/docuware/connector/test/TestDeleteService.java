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
  public void deleteDocument(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app)
      throws IOException {
    prepareRestClient(app, fixture);
    ExecutionResult result = bpmClient.start().subProcess(testeeDelete)
        .withParam("documentId", String.valueOf(DOCUMENT_ID)).execute();
    DeleteServiceData data = result.data().last();
    assertThat(data.getError()).isNull();
  }

  @Test
  public void deleteDocumentError(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app)
      throws IOException {
    prepareRestClient(app, fixture);
    ExecutionResult result = bpmClient.start().subProcess(testeeDelete)
        .withParam("documentId", Constants.DOCUMENT_ID_ERRROR_CASE).execute();
    DeleteServiceData data = result.data().last();
    assertThat(data.getError()).isNotNull();
    assertThat(data.getError().getErrorMessage()).isNotEmpty();
  }

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
