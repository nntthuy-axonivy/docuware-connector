package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.security.ISession;

@IvyProcessTest(enableWebServer = true)
public class TestDeleteService extends TestDocuWareConnector {
	private static final BpmElement testeeDelete = BpmProcess.path("DeleteService").elementName("deleteDocument(String, String, String)");

	@Test
	public void deleteDocument(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app) throws IOException {
		prepareRestClient(app, fixture);

		bpmClient.start()
		.subProcess(testeeDelete)
		.withParam("configKey", Constants.CONFIG_KEY)
		.withParam("documentId", Constants.DOCUMENT_ID_OK)
		.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
		.execute();
	}

	@Test
	public void deleteDocumentError(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app) throws IOException {
		prepareRestClient(app, fixture);

		assertThatExceptionOfType(BpmError.class).isThrownBy(() ->
		bpmClient.start()
		.subProcess(testeeDelete)
		.withParam("configKey", Constants.CONFIG_KEY)
		.withParam("documentId", Constants.DOCUMENT_ID_ERROR)
		.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
		.execute()).extracting(BpmError::getErrorCode).isEqualTo("ivy:error:rest:client");
	}
}
