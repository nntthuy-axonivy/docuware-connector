package com.axonivy.connector.docuware.connector.demo.ui;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import com.axonivy.connector.docuware.connector.DocuWareProperties;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.demo.enums.ItemType;
import com.docuware.dev.schema._public.services.platform.Document;
import com.docuware.dev.schema._public.services.platform.FileCabinet;

import ch.ivyteam.ivy.environment.Ivy;

public class DocumentTableCtrl implements Serializable {
	private static final long serialVersionUID = 4548574141754643263L;

	private static final String CMS_LABELS_SUCCESS = "/Labels/Success";
	private static final String CMS_LABELS_FAILED = "/Labels/Failed";
	private static final String CMS_SUCCESS_EDIT_MESSAGE = "/Dialogs/com/axonivy/connector/docuware/connector/demo/DocumentTable/successEditMessage";
	private static final String CMS_FAILED_EDIT_MESSAGE = "/Dialogs/com/axonivy/connector/docuware/connector/demo/DocumentTable/failedEditMessage";

	private static final String TOKEN_PARAM = "token";
	private static final String FILE_CABINET_ID_PARAM = "fc";
	private static final String DOCUMENT_ID_PARAM = "did";
	private static final String DOCUMENT = "Document";
	private static final String CLIENT = "Client";
	private static final String WEB_CLIENT = "WebClient";

	private FileCabinet fileCabinet;
	private List<Document> documents;
	private String documentId;
	private DocuWareProperties properties;
	private String documentUrl;

	public void buildDocumentUrl(String documentId, String fileCabinetId) throws URISyntaxException {
		var token = DocuWareService.get().getLoginTokenString(null);
		documentUrl =
				DocuWareService.get().createUriBuilder(null, WEB_CLIENT, CLIENT, DOCUMENT)
				.addParameter(DOCUMENT_ID_PARAM, documentId)
				.addParameter(FILE_CABINET_ID_PARAM, fileCabinetId)
				.addParameter(TOKEN_PARAM, token)
				.build()
				.toString();
		Ivy.log().info("Created document URL: {0}", documentUrl);
	}

	public boolean isFieldNameTypeNumber(DocuWareProperty field) {
		return field.getItemElementName().equals(ItemType.DECIMAL.getValue())
				|| field.getItemElementName().equals(ItemType.INT.getValue());
	}

	public boolean isFieldNameTypeString(DocuWareProperty field) {
		return field.getItemElementName().equals(ItemType.STRING.getValue());
	}

	public void updateGrowlMessageForEditAction(boolean isUpdateSuccess) {
		if (isUpdateSuccess) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, Ivy.cms().co(CMS_LABELS_SUCCESS), Ivy.cms().co(
							CMS_SUCCESS_EDIT_MESSAGE)));
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, Ivy.cms().co(CMS_LABELS_FAILED), Ivy.cms().co(
							CMS_FAILED_EDIT_MESSAGE)));
		}
	}

	public FileCabinet getFileCabinet() {
		return fileCabinet;
	}

	public void setFileCabinet(FileCabinet fileCabinet) {
		this.fileCabinet = fileCabinet;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public DocuWareProperties getProperties() {
		return properties;
	}

	public void setProperties(DocuWareProperties properties) {
		this.properties = properties;
	}

	public String getDocumentUrl() {
		return documentUrl;
	}
}
