package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.axonivy.market.docuware.connector.UpdateServiceData;

import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;

@IvyProcessTest(enableWebServer = true)
public class UpdateServiceTest extends DocuWareConnectorTest {
	private static final BpmElement UPDATE_DOCUMENT_SP = BpmProcess.path("UpdateService").elementName("updateDocument(String, String, String, List<DocuWareProperty>)");

	@Test
	public void updateDocumentWithEndpointConfiguration(BpmClient bpmClient) throws IOException {
		var propertyList = prepareDocuWareProperties();
		var result = bpmClient.start().subProcess(UPDATE_DOCUMENT_SP)
				.withParam("configKey", Constants.CONFIG_KEY)
				.withParam("documentId", Constants.DOCUMENT_ID_OK)
				.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
				.withParam("indexFields", propertyList)
				.execute();
		UpdateServiceData data = result.data().last();
		assertThat(data.getDocumentIndexFields()).isNotNull();
		assertThat(data.getDocumentIndexFields().getField()).isNotEmpty();
	}
}
