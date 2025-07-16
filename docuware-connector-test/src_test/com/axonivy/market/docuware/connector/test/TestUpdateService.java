package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.market.docuware.connector.UpdateServiceData;
import com.axonivy.market.docuware.connector.constants.DocuwareTestConstants;
import com.axonivy.market.docuware.connector.utils.DocuwareTestUtils;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.scripting.objects.List;

@IvyProcessTest(enableWebServer = true)
public class TestUpdateService extends TestDocuWareConnector {

  private static final int DOCUMENT_ID = 1;
  private static final BpmElement testeeUpdate = BpmProcess.path("UpdateService").elementName("updateDocument");
  private static final BpmElement testeeUpdate_2 = BpmProcess.path("UpdateService")
      .elementName("updateDocument(Integer, List<DocuWareProperty>, DocuWareEndpointConfiguration)");

  @TestTemplate
  public void updateDocument(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    ExecutionResult result = bpmClient.start().subProcess(testeeUpdate)
        .withParam("documentId", String.valueOf(DOCUMENT_ID)).withParam("indexFields", propertyList).execute();
    UpdateServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getDocumentIndexFields()).isNotNull();
      assertThat(data.getDocumentIndexFields().getField()).isNotEmpty();
    }
  }

  @TestTemplate
  public void updateDocumentWithEndpointConfiguration(BpmClient bpmClient, ExtensionContext context)
      throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    DocuWareEndpointConfiguration configuration = DocuwareTestUtils.prepareDocuWareEndpointConfiguration();
    ExecutionResult result =
        bpmClient.start().subProcess(testeeUpdate_2).withParam("documentId", String.valueOf(DOCUMENT_ID))
            .withParam("indexFields", propertyList).withParam("configuration", configuration).execute();
    UpdateServiceData data = result.data().last();
    if (isRealCall) {
      assertServiceErrorCodeIs404(data,
          d -> (int) d.getError().getAttribute(DocuwareTestConstants.REST_CLIENT_RESPONSE_STATUS_CODE));
    } else {
      assertThat(data.getDocumentIndexFields()).isNotNull();
      assertThat(data.getDocumentIndexFields().getField()).isNotEmpty();
    }
  }
}
