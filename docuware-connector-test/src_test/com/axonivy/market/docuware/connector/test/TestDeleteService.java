package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.market.docuware.connector.DeleteServiceData;
import com.axonivy.market.docuware.connector.constants.DocuwareTestConstants;
import com.axonivy.market.docuware.connector.utils.DocuwareTestUtils;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;

@IvyProcessTest(enableWebServer = true)
public class TestDeleteService extends TestDocuWareConnector {

  private static final int DOCUMENT_ID = 2;
  private static final BpmElement testeeDelete = BpmProcess.path("DeleteService").elementName("deleteDocument(String)");
  private static final BpmElement testeeDelete_2 =
      BpmProcess.path("DeleteService").elementName("deleteDocument(String, DocuWareEndpointConfiguration)");

  @TestTemplate
  public void deleteDocument(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    ExecutionResult result =
        bpmClient.start().subProcess(testeeDelete).withParam("documentId", String.valueOf(DOCUMENT_ID)).execute();
    DeleteServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getError()).isNull();
    }
  }

  @TestTemplate
  public void deleteDocumentError(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    ExecutionResult result =
        bpmClient.start().subProcess(testeeDelete).withParam("documentId", Constants.DOCUMENT_ID_ERRROR_CASE).execute();
    DeleteServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getError()).isNotNull();
      assertThat(data.getError().getErrorMessage()).isNotEmpty();
    }

  }

  @TestTemplate
  public void deleteDocumentWithEndpointConfiguration(BpmClient bpmClient, ExtensionContext context)
      throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    DocuWareEndpointConfiguration configuration = DocuwareTestUtils.prepareDocuWareEndpointConfiguration();
    ExecutionResult result = bpmClient.start().subProcess(testeeDelete_2)
        .withParam("documentId", String.valueOf(DOCUMENT_ID)).withParam("configuration", configuration).execute();
    DeleteServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getError()).isNull();
    }
  }
}
