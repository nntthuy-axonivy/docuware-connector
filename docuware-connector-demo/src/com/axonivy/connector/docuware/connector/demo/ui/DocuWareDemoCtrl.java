package com.axonivy.connector.docuware.connector.demo.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.axonivy.connector.docuware.connector.DocuWareCheckInActionParameters;
import com.axonivy.connector.docuware.connector.DocuWareProperties;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.docuware.dev.schema._public.services.platform.CheckInReturnDocument;
import com.docuware.dev.schema._public.services.platform.Document;
import com.docuware.dev.schema._public.services.platform.DocumentIndexField;
import com.docuware.dev.schema._public.services.platform.DocumentsQueryResult;
import com.docuware.dev.schema._public.services.platform.FileCabinets;
import com.docuware.dev.schema._public.services.platform.Organizations;

import ch.ivyteam.ivy.environment.Ivy;

public class DocuWareDemoCtrl {
	private StringWriter stringWriter = new StringWriter();
	private PrintWriter printWriter = new PrintWriter(stringWriter);
	private String loginToken;
	private String organizationId;
	private String fileCabinetId;
	private String documentId;
	private Organizations organizations;
	private FileCabinets fileCabinets;
	private DocumentsQueryResult documents;
	private Document document;
	private StreamedContent downloadedFile;
	private String viewerUrl;
	private String resultListUrl;
	private String cryptIn;
	private String cryptOut;
	private byte[] checkedOut;
	private InputStream checkedOutStream;
	private String checkedOutFilename;
	private List<Field> fields;
	private static final Random RND = new Random();

	public DocuWareDemoCtrl() {
		organizationId = Ivy.var().get("docuwareConnector.organization");
		fileCabinetId = Ivy.var().get("docuwareConnector.filecabinetid");
		fields = new ArrayList<>();
		fields.add(Field.create("SUBJECT", "Ivy Test File %s".formatted(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))));
		fields.add(Field.create("STATUS", "neu"));
		fields.add(Field.create("", ""));
		fields.add(Field.create("", ""));
		fields.add(Field.create("", ""));
	}

	public String getMessage() {
		return stringWriter != null ? stringWriter.toString() : "";
	}

	public String getConfigurationDescription() {
		try (var sw = new StringWriter();
				var pw = new PrintWriter(sw)) {
			pw.format(
					"""
					Global variables:
					Host: '%s'
					Platform: '%s'
					GrantType: '%s'
					Username: '%s'
					Password: '%s'
					TrustedUserName: '%s'
					TrustedUserPassword: '%s'
					""",
					DocuWareVariable.HOST.getValue(),
					DocuWareVariable.PLATFORM.getValue(),
					DocuWareVariable.GRANT_TYPE.getValue(),
					DocuWareVariable.USERNAME.getValue(),
					safeShow(DocuWareVariable.PASSWORD.getValue()),
					DocuWareVariable.TRUSTED_USERNAME.getValue(),
					safeShow(DocuWareVariable.TRUSTED_USER_PASSWORD.getValue())
					);
			return sw.toString();
		} catch (IOException e) {
			log("Error reading configuration.", e);
		}
		return "Could not read configuration.";
	}

	public Organizations getOrganizations() {
		return organizations;
	}

	public void setOrganizations(Organizations organizations) {
		this.organizations = organizations;
		if(organizations != null) {
			var orgs = organizations.getOrganization();
			if(orgs != null && !orgs.isEmpty()) {
				organizationId = orgs.get(0).getId();
			}
		}
	}

	public FileCabinets getFileCabinets() {
		return fileCabinets;
	}

	public void setFileCabinets(FileCabinets fileCabinets) {
		this.fileCabinets = fileCabinets;
		if(fileCabinets != null) {
			var fcs = fileCabinets.getFileCabinet();
			if(fcs != null && !fcs.isEmpty()) {
				fileCabinetId = fcs.get(0).getId();
			}
		}
	}

	public DocumentsQueryResult getDocuments() {
		return documents;
	}

	public void setDocuments(DocumentsQueryResult documents) {
		this.documents = documents;
		if(documents != null && documents.getItems() != null) {
			var docs = documents.getItems().getItem();
			if(docs != null && !docs.isEmpty()) {
				documentId = ""+docs.get(0).getId();
			}
		}
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
		if(document != null) {
			documentId = "" + document.getId();
		}
	}

	public boolean hasAccessToken() {
		return DocuWareService.get().getCachedToken() != null;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public String getFileCabinetId() {
		return fileCabinetId;
	}

	public void setFileCabinetId(String fileCabinetId) {
		this.fileCabinetId = fileCabinetId;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public StreamedContent getDownloadedFile() {
		return downloadedFile;
	}

	public String getViewerUrl() {
		return viewerUrl;
	}
	
	public String getResultListUrl() {
		return resultListUrl;
	}

	public String getCryptIn() {
		return cryptIn;
	}

	public void setCryptIn(String cryptIn) {
		this.cryptIn = cryptIn;
	}

	public String getCryptOut() {
		return cryptOut;
	}

	public void setCryptOut(String cryptOut) {
		this.cryptOut = cryptOut;
	}

	public byte[] getCheckedOut() {
		return checkedOut;
	}

	public void setCheckedOut(byte[] checkedOut) {
		this.checkedOut = checkedOut;
	}

	public String getCheckedOutFilename() {
		return checkedOutFilename;
	}

	public void setCheckedOutFilename(String checkedOutFilename) {
		this.checkedOutFilename = checkedOutFilename;
	}

	public InputStream getCheckedOutStream() {
		return checkedOutStream;
	}

	public void setCheckedOutStream(InputStream checkedOutStream) {
		this.checkedOutStream = checkedOutStream;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public boolean isIntegrationPasswordSet() {
		return StringUtils.isNotBlank(DocuWareVariable.INTEGRATION_PASSPHRASE.getValue());
	}

	public void prepareDownloadedFile(Response response, InputStream result) throws IOException {
		if(response != null && response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
			var content = new ByteArrayInputStream(result.readAllBytes());
			downloadedFile = DefaultStreamedContent.builder()
					.stream(() -> content)
					.name(DocuWareService.get().getFilenameFromResponseHeader(response))
					.build();
		}
	}

	public void handleCheckoutResult(Response response, InputStream result) throws IOException {
		log("Checked out");

		checkedOut = result.readAllBytes();
		checkedOutFilename = DocuWareService.get().getFilenameFromResponseHeader(response);

		log("Loaded {0} bytes of file ''{1}''", checkedOut.length, checkedOutFilename);

	}

	public DocuWareCheckInActionParameters createCheckInActionParameters() {
		var version = document.getVersion();

		var maj = version.getMajor();
		var min = version.getMinor();

		if(RND.nextInt() % 10 < 7) {
			min++;
		}
		else {
			min = 0;
			maj++;
		}

		return DocuWareService.get().createCheckInActionParameters(
				CheckInReturnDocument.CHECKED_IN,
				"Checked in by AxonIvy at %s".formatted(LocalDateTime.now()),
				maj, min);
	}

	public DocuWareProperties createProperties() {
		var props = new DocuWareProperties();

		props.setProperties(new ArrayList<>());

		for (var field : fields) {
			if(StringUtils.isNotBlank(field.getName())) {
				props.getProperties().add(new DocuWareProperty(field.getName(), field.getValue(), "String"));
			}
		}

		return props;
	}

	private String safeShow(String sensitive) {
		return sensitive == null ? null : sensitive.replaceAll(".", "*");
	}

	public String buildViewerUrl() {
		var dwService = DocuWareService.get();
		var loginToken = dwService.getLoginTokenString();
		viewerUrl = dwService.getViewerUrl(null, loginToken, fileCabinetId, documentId);
		return viewerUrl;
	}

	public String buildCabinetResultListAndViewerUrl() {
		var dwService = DocuWareService.get();
		var loginToken = dwService.getLoginTokenString();
		resultListUrl = dwService.getCabinetResultListAndViewerUrl(null, loginToken, fileCabinetId);
		return resultListUrl;
	}
	
	public void log(String format, Object...params) {
		if(params.length > 0) {
			Ivy.log().info(format, params);
			printWriter.println(MessageFormat.format(format, params));
		}
		else {
			Ivy.log().info(format);
			printWriter.println(format);
		}
	}

	public void log(String format, Throwable t, Object...params) {
		log(format, params);
		log(ExceptionUtils.getStackTrace(t));
	}


	public void log(Organizations organizations) {
		log("Organizations:");
		if(organizations == null) {
			log("null");
		}
		else {
			var orgs = organizations.getOrganization();
			log("Size: {0}", orgs != null ? orgs.size() : orgs);
			if(orgs != null) {
				for (var org : orgs) {
					log("Id: {0} - ''{1}'', Guid: {2}", org.getId(), org.getName(), org.getGuid());
				}
			}
		}
	}

	public void log(FileCabinets fileCabinets) {
		log("File Cabinets:");
		if(fileCabinets == null) {
			log("null");
		}
		else {
			var fcs = fileCabinets.getFileCabinet();
			log("Size: {0}", fcs != null ? fcs.size() : fcs);
			if(fcs != null) {
				for (var fc : fcs) {
					log("Id: {0} - ''{1}''", fc.getId(), fc.getName());
				}
			}
		}
	}

	public void log(DocumentsQueryResult documents) {
		log("Documents:");
		if(documents == null || documents.getItems() == null) {
			log("null");
		}
		else {
			var docs = documents.getItems().getItem();
			log("Size: {0}", docs != null ? docs.size() : docs);
			if(docs != null) {
				for (var doc : docs) {
					log("Id: {0} - ''{1}''", doc.getId(), doc.getTitle());
				}
			}
		}
	}

	public void log(Document document) {
		if(document == null) {
			log("Document: null");
		}
		else {
			log("Document: Id: {0} - ''{1}'' Version: {2}.{3}", document.getId(), document.getTitle(), document.getVersion().getMajor(), document.getVersion().getMinor());
			for(var field : document.getFields().getField()) {
				log("Field: {0} ({1}) Value: {2}", field.getFieldLabel(), field.getFieldName(), toString(field));
			}
		}
	}
	
	private String toString(DocumentIndexField field) {
		String result = field.toString();
		if(field.getDate() != null) {
			result = field.getDate().toString();
		}
		else if(field.getDateTime() != null) {
			result = field.getDateTime().toString();
		}
		else if(field.getDecimal() != null) {
			result = field.getDecimal().toString();
		}
		else if(field.getInt() != null) {
			result = field.getInt().toString();
		}
		else if(field.getKeywords() != null) {
			result = field.getKeywords().toString();
		}
		else if(field.getMemo() != null) {
			result = field.getMemo().toString();
		}
		else if(field.getPointAndShootInfo() != null) {
			result = field.getPointAndShootInfo().toString();
		}
		else if(field.getString() != null) {
			result = field.getString().toString();
		}
		else if(field.getTable() != null) {
			result = field.getTable().toString();
		}
		return result;
	}

	public void clearLog() {
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
	}

	public static class Field {
		private String name;
		private String value;

		public static Field create(String name, String value) {
			var field = new Field();
			field.setName(name);
			field.setValue(value);
			return field;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
