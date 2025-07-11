package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.demo.service.DocuWareDemoService;
import com.axonivy.market.docuware.connector.UploadServiceData;

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
public class TestUploadService extends TestDocuWareConnector {

	private static final BpmElement testeeUploadFile_2 = BpmProcess.path("UploadService")
			.elementName("uploadFileWithIndexFields(File, List<DocuWareProperty>, DocuWareEndpointConfiguration)");
	private static final BpmElement testeeUploadFile_4 = BpmProcess.path("UploadService").elementName(
			"uploadFileWithIndexFields(InputStream,String,List<DocuWareProperty>,DocuWareEndpointConfiguration)");

	@Test
	public void uploadFileWithEndpointConfiguration(BpmClient bpmClient, ISession session, AppFixture fixture,
			IApplication app) throws IOException {
		prepareRestClient(app, fixture);
		List<DocuWareProperty> propertyList = prepareDocuWareProperties();
		DocuWareEndpointConfiguration configuration = prepareDocuWareEndpointConfiguration();
		File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
		ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_2).withParam("indexFields", propertyList)
				.withParam("file", pdf).withParam("configuration", configuration).execute();
		UploadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
	}

	@Test
	public void uploadFileWithEndpointConfigurationWithStoreDialogFromVariable(BpmClient bpmClient, ISession session,
			AppFixture fixture, IApplication app) throws IOException {
		fixture.var("docuwareConnector.storedialogid", "" + Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_1);
		prepareRestClient(app, fixture);
		List<DocuWareProperty> propertyList = prepareDocuWareProperties();
		DocuWareEndpointConfiguration configuration = prepareDocuWareEndpointConfiguration();
		File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
		ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_2).withParam("indexFields", propertyList)
				.withParam("file", pdf).withParam("configuration", configuration).execute();
		UploadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_1);
	}

	@Test
	public void uploadFileWithEndpointConfigurationWithCustomStoreDialog(BpmClient bpmClient, ISession session,
			AppFixture fixture, IApplication app) throws IOException {
		prepareRestClient(app, fixture);
		List<DocuWareProperty> propertyList = prepareDocuWareProperties();
		DocuWareEndpointConfiguration configuration = prepareDocuWareEndpointConfiguration();
		configuration.setStoreDialogId("" + Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_2);
		File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
		ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_2).withParam("indexFields", propertyList)
				.withParam("file", pdf).withParam("configuration", configuration).execute();
		UploadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_2);
	}

	@Test
	public void uploadFileStreamWithEndpointConfiguration(BpmClient bpmClient, ISession session, AppFixture fixture,
			IApplication app) throws IOException {
		prepareRestClient(app, fixture);
		List<DocuWareProperty> propertyList = prepareDocuWareProperties();
		DocuWareEndpointConfiguration configuration = prepareDocuWareEndpointConfiguration();
		File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
		ExecutionResult result = bpmClient.start().subProcess(testeeUploadFile_4).withParam("indexFields", propertyList)
				.withParam("fileStream", new FileInputStream(pdf)).withParam("filename", "MyFile").withParam("configuration", configuration)
				.execute();
		UploadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Constants.EXPECTED_DOCUMENT_ID);
	}
}
