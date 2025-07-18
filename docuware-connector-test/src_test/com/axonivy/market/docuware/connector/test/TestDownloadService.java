package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.market.docuware.connector.DownloadServiceData;
import com.axonivy.market.docuware.connector.constants.DocuwareTestConstants;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;

@IvyProcessTest(enableWebServer = true)
public class TestDownloadService extends TestDocuWareConnector {

  private static final BpmElement testeeDownload = BpmProcess.path("DownloadService").elementName("getDocument");
  private static final BpmElement testeeDownload_2 =
      BpmProcess.path("DownloadService").elementName("getDocument(String,DocuWareEndpointConfiguration)");
  private static final BpmElement testeeDownload_3 =
      BpmProcess.path("DownloadService").elementName("downloadFile(String)");
  private static final BpmElement testeeDownload_4 =
      BpmProcess.path("DownloadService").elementName("downloadFile(String,DocuWareEndpointConfiguration)");

  @TestTemplate
  public void downloadDocument(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    ExecutionResult result = bpmClient.start().subProcess(testeeDownload)
        .withParam("documentId", String.valueOf(Constants.EXPECTED_DOCUMENT_ID)).execute();
    DownloadServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
    }
  }

  @TestTemplate
  public void downloadDocumentWithEndpointConfiguration(BpmClient bpmClient, ExtensionContext context)
      throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    ExecutionResult result = bpmClient.start().subProcess(testeeDownload_2)
        .withParam("documentId", String.valueOf(Constants.EXPECTED_DOCUMENT_ID)).execute();
    DownloadServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
    }
  }

  @TestTemplate
  public void downloadFile(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    DocuWareEndpointConfiguration configuration = new DocuWareEndpointConfiguration();
    ExecutionResult result = bpmClient.start().subProcess(testeeDownload_3)
        .withParam("documentId", String.valueOf(Constants.EXPECTED_DOCUMENT_ID))
        .withParam("configuration", configuration).execute();
    DownloadServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getFile()).isNotNull();
      assertThat(data.getFile().getName()).isEqualTo(Constants.EXPECTED_FILE_NAME);
    }
  }

  @TestTemplate
  public void downloadFileWithEndpointConfiguration(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    DocuWareEndpointConfiguration configuration = new DocuWareEndpointConfiguration();
    ExecutionResult result = bpmClient.start().subProcess(testeeDownload_4)
        .withParam("documentId", String.valueOf(Constants.EXPECTED_DOCUMENT_ID))
        .withParam("configuration", configuration).execute();
    DownloadServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getFile()).isNotNull();
      assertThat(data.getFile().getName()).isEqualTo(Constants.EXPECTED_FILE_NAME);
    }
  }
}
