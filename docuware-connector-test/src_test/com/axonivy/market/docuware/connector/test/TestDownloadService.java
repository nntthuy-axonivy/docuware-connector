package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.market.docuware.connector.DownloadServiceData;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.security.ISession;

@IvyProcessTest(enableWebServer = true)
public class TestDownloadService extends TestDocuWareConnector {


  @Test
  public void downloadDocumentWithEndpointConfiguration(BpmClient bpmClient, ISession session, AppFixture fixture,
      IApplication app) throws IOException {
    prepareRestClient(app, fixture);
    var testeeDownload_2 = BpmProcess.path("DownloadService")
    		.elementName("getDocument(String,DocuWareEndpointConfiguration)");
    ExecutionResult result = bpmClient.start().subProcess(testeeDownload_2)
        .withParam("documentId", String.valueOf(Constants.EXPECTED_DOCUMENT_ID)).execute();
    DownloadServiceData data = result.data().last();
    assertThat(data.getDocument()).isNotNull();
    assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
  }

  @Test
  public void downloadFileWithEndpointConfiguration(BpmClient bpmClient, ISession session, AppFixture fixture,
      IApplication app) throws IOException {
    prepareRestClient(app, fixture);
    DocuWareEndpointConfiguration configuration = prepareDocuWareEndpointConfiguration();
    var testeeDownload_4 = BpmProcess.path("DownloadService")
    		.elementName("downloadFile(String,DocuWareEndpointConfiguration)");
    ExecutionResult result = bpmClient.start().subProcess(testeeDownload_4)
        .withParam("documentId", String.valueOf(Constants.EXPECTED_DOCUMENT_ID))
        .withParam("configuration", configuration).execute();
    DownloadServiceData data = result.data().last();
    assertThat(data.getFile()).isNotNull();
    assertThat(data.getFile().getName()).isEqualTo(Constants.EXPECTED_FILE_NAME);
  }
}
