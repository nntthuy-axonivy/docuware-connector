package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.axonivy.market.docuware.connector.DownloadServiceData;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.security.ISession;

@IvyProcessTest(enableWebServer = true)
public class DownloadServiceTest extends DocuWareConnectorTest {
	private static final BpmElement GET_DOCUMENT_SP = BpmProcess.path("DownloadService").elementName("getDocument(String,String,String)");
	private static final BpmElement DOWNLOAD_SP = BpmProcess.path("DownloadService").elementName("downloadFile(String,String,String)");

	@Test
	public void downloadDocument(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app) throws IOException {
		// prepareRestClient(app, fixture);

		var result = bpmClient.start()
				.subProcess(GET_DOCUMENT_SP)
				.withParam("configKey", Constants.CONFIG_KEY)
				.withParam("documentId", Constants.EXPECTED_DOCUMENT_ID)
				.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
				.execute();
		DownloadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Integer.valueOf(Constants.EXPECTED_DOCUMENT_ID));
	}

	@Test
	public void downloadFile(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app) throws IOException {
		// prepareRestClient(app, fixture);

		var result = bpmClient.start()
				.subProcess(DOWNLOAD_SP)
				.withParam("configKey", Constants.CONFIG_KEY)
				.withParam("documentId", Constants.EXPECTED_DOCUMENT_ID)
				.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
				.execute();
		DownloadServiceData data = result.data().last();
		assertThat(data.getFile()).isNotNull();
		assertThat(data.getFile().getName()).isEqualTo(Constants.EXPECTED_FILE_NAME);
	}
}
