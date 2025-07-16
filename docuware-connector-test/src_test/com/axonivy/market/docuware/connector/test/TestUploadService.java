package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.demo.service.DocuWareDemoService;
import com.axonivy.market.docuware.connector.DeleteServiceData;
import com.axonivy.market.docuware.connector.UploadServiceData;
import com.axonivy.market.docuware.connector.constants.DocuwareTestConstants;
import com.axonivy.market.docuware.connector.utils.DocuwareTestUtils;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.scripting.objects.List;

@IvyProcessTest(enableWebServer = true)
public class TestUploadService extends TestDocuWareConnector{

  private static final BpmElement testeeUploadFile_1 =
      BpmProcess.path("UploadService").elementName("uploadFileWithIndexFields(File,List<DocuWareProperty>)");
  private static final BpmElement testeeUploadFile_2 = BpmProcess.path("UploadService")
      .elementName("uploadFileWithIndexFields(File, List<DocuWareProperty>, DocuwareConfiguration)");
  private static final BpmElement testeeUploadFile_3 = BpmProcess.path("UploadService")
      .elementName("uploadFileWithIndexFields(List<Byte>, List<DocuWareProperty>, String)");
  private static final BpmElement testeeUploadFile_4 = BpmProcess.path("UploadService").elementName(
      "uploadFileWithIndexFields(List<Byte>, List<DocuWareProperty>, String, DocuWareEndpointConfiguration)");

  private static final BpmElement testeeDelete = BpmProcess.path("DeleteService").elementName("deleteDocument(String)");

  @TestTemplate
  public void uploadFile(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
    ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_1).withParam("indexFields", propertyList)
        .withParam("file", pdf).execute();
    UploadServiceData data = result.data().last();
    if (isRealCall) {
      deleteUploadedDocument(bpmClient, data);
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
    }
  }

  @TestTemplate
  public void uploadFileWithEndpointConfiguration(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    DocuWareEndpointConfiguration configuration = DocuwareTestUtils.prepareDocuWareEndpointConfiguration();
    File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
    ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_2).withParam("indexFields", propertyList)
        .withParam("file", pdf).withParam("configuration", configuration).execute();
    UploadServiceData data = result.data().last();
    if (isRealCall) {
      deleteUploadedDocument(bpmClient, data);
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
    }
  }

  @TestTemplate
  public void uploadFileWithEndpointConfigurationWithStoreDialogFromVariable(BpmClient bpmClient,
      ExtensionContext context, AppFixture fixture) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    DocuWareEndpointConfiguration configuration = DocuwareTestUtils.prepareDocuWareEndpointConfiguration();
    if (!isRealCall) {
      fixture.var("docuwareConnector.storedialogid", "" + Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_1);
      configuration.setStoreDialogId("" + Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_1);
    }
    File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
    ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_2).withParam("indexFields", propertyList)
        .withParam("file", pdf).withParam("configuration", configuration).execute();
    UploadServiceData data = result.data().last();
    if (isRealCall) {
      deleteUploadedDocument(bpmClient, data);
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_1);
    }
  }

  @TestTemplate
  public void uploadFileWithEndpointConfigurationWithCustomStoreDialog(BpmClient bpmClient, ExtensionContext context)
      throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    DocuWareEndpointConfiguration configuration = DocuwareTestUtils.prepareDocuWareEndpointConfiguration();
    if (!isRealCall) {
      configuration.setStoreDialogId("" + Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_2);
    }
    File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
    ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_2).withParam("indexFields", propertyList)
        .withParam("file", pdf).withParam("configuration", configuration).execute();
    UploadServiceData data = result.data().last();
    if (isRealCall) {
      deleteUploadedDocument(bpmClient, data);
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_2);
    }
  }

  @TestTemplate
  public void uploadFileStream(BpmClient bpmClient, ExtensionContext context) throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
    byte[] bytes = Files.readAllBytes(pdf.toPath());
    java.util.List<Byte> byteList = Arrays.asList(ArrayUtils.toObject(bytes));
    ch.ivyteam.ivy.scripting.objects.List<java.lang.Byte> bytesAsList = new List<java.lang.Byte>();
    bytesAsList.addAll(byteList);
    ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_3).withParam("indexFields", propertyList)
        .withParam("file", bytesAsList).withParam("filename", "MyFile").execute();
    UploadServiceData data = result.data().last();
    if (isRealCall) {
      deleteUploadedDocument(bpmClient, data);
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
    }
  }

  @TestTemplate
  public void uploadFileStreamWithEndpointConfiguration(BpmClient bpmClient, ExtensionContext context)
      throws IOException {
    boolean isRealCall = context.getDisplayName().equals(DocuwareTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME);
    List<DocuWareProperty> propertyList = DocuwareTestUtils.prepareDocuWareProperties();
    DocuWareEndpointConfiguration configuration = DocuwareTestUtils.prepareDocuWareEndpointConfiguration();
    File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
    byte[] bytes = Files.readAllBytes(pdf.toPath());
    java.util.List<Byte> byteList = Arrays.asList(ArrayUtils.toObject(bytes));
    ch.ivyteam.ivy.scripting.objects.List<java.lang.Byte> bytesAsList = new List<java.lang.Byte>();
    bytesAsList.addAll(byteList);
    ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_4).withParam("indexFields", propertyList)
        .withParam("file", bytesAsList).withParam("filename", "MyFile").withParam("configuration", configuration)
        .execute();
    UploadServiceData data = result.data().last();
    if (isRealCall) {
      deleteUploadedDocument(bpmClient, data);
    } else {
      assertThat(data.getDocument()).isNotNull();
      assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
    }
  }

  private void deleteUploadedDocument(BpmClient bpmClient, UploadServiceData data) {
    assertNotNull(data.getDocument().getId());
    ExecutionResult deleteResult = bpmClient.start().subProcess(testeeDelete)
        .withParam("documentId", String.valueOf(data.getDocument().getId())).execute();
    DeleteServiceData deletedData = deleteResult.data().last();
    assertThat(deletedData.getError()).isNull();
    assertThat(deletedData.getDocumentId()).isEqualTo(String.valueOf(data.getDocument().getId()));
  }
}
