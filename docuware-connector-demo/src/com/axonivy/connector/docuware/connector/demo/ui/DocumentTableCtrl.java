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

	private FileCabinet fileCabinet;
	private List<Document> documents;
	private String documentId;
	private DocuWareProperties properties;

	private String documentUrl;

	public void buildDocumentUrl(String documentId, String fileCabinetId) throws URISyntaxException {
		var token = DocuWareService.get().getLoginTokenString(null);
		documentUrl =
				DocuWareService.get().createUriBuilder(null, "WebClient", "Client", "Document")
				.addParameter("did", documentId)
				.addParameter("fc", fileCabinetId)
				.addParameter("token", token)
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
					new FacesMessage(FacesMessage.SEVERITY_INFO, Ivy.cms().co("/Labels/Success"), Ivy.cms().co(
							"/Dialogs/com/axonivy/connector/docuware/connector/demo/DocumentTable/successEditMessage")));
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, Ivy.cms().co("/Labels/Failed"), Ivy.cms().co(
							"/Dialogs/com/axonivy/connector/docuware/connector/demo/DocumentTable/failedEditMessage")));
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
