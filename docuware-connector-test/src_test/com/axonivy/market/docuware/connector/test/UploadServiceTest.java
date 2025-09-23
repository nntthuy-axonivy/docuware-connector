package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.axonivy.market.docuware.connector.UploadServiceData;

import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.scripting.objects.File;

@IvyProcessTest(enableWebServer = true)
public class UploadServiceTest extends DocuWareConnectorTest {
	private static final String FILE_PATH = "/Files/testfile";
	private static final BpmElement UPDLOAD_FILE_SP = BpmProcess.path("UploadService").elementName("uploadFileWithIndexFields(String,String,File,List<DocuWareProperty>,String)");
	private static final BpmElement UPLOAD_STREAM_SP = BpmProcess.path("UploadService").elementName("uploadFileWithIndexFields(String,String,InputStream,String,List<DocuWareProperty>,String)");

	private File exportFromCms(String path) {
		try (var out = new FileWriter(new File("tmp-%d.pdf".formatted(System.currentTimeMillis()), true).getJavaFile())) {
			Ivy.cms().get(path).get().value().get().read().reader().transferTo(out);
			return null;
		} catch (IOException e) {
			throw new RuntimeException("Could not create file form CMS path '%s'".formatted(path));
		}
	}

	@Test
	public void uploadFile(BpmClient bpmClient) throws IOException {
		var propertyList = prepareDocuWareProperties();

		var pdf = exportFromCms(FILE_PATH);
		var result = bpmClient.start()
				.subProcess(UPDLOAD_FILE_SP)
				.withParam("configKey", Constants.CONFIG_KEY)
				.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
				.withParam("file", pdf)
				.withParam("indexFields", propertyList)
				.execute();
		UploadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Integer.valueOf(Constants.EXPECTED_DOCUMENT_ID));
	}

	@Test
	public void uploadStream(BpmClient bpmClient) throws IOException {
		var propertyList = prepareDocuWareProperties();

		var stream = Ivy.cms().get(FILE_PATH).get().value().get().read().inputStream();
		var result = bpmClient.start()
				.subProcess(UPLOAD_STREAM_SP)
				.withParam("configKey", Constants.CONFIG_KEY)
				.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
				.withParam("fileStream", stream)
				.withParam("fileName", "test.pdf")
				.withParam("indexFields", propertyList)
				.execute();

		UploadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Integer.valueOf(Constants.EXPECTED_DOCUMENT_ID));
	}

	@Test
	public void uploadStreamWithStoreDialog(BpmClient bpmClient) throws IOException {
		var propertyList = prepareDocuWareProperties();

		var stream = Ivy.cms().get(FILE_PATH).get().value().get().read().inputStream();
		var result = bpmClient.start()
				.subProcess(UPLOAD_STREAM_SP)
				.withParam("configKey", Constants.CONFIG_KEY)
				.withParam("fileCabinetId", Constants.FILE_CABINET_ID_OK)
				.withParam("fileStream", stream)
				.withParam("fileName", "test.pdf")
				.withParam("indexFields", propertyList)
				.withParam("storeDialogId", Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG)
				.execute();
		UploadServiceData data = result.data().last();
		assertThat(data.getDocument()).isNotNull();
		assertThat(data.getDocument().getId()).isEqualTo(Integer.valueOf(Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG));
	}
}
