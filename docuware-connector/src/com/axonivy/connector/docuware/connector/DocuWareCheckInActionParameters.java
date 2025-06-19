package com.axonivy.connector.docuware.connector;
import java.io.IOException;

import com.docuware.dev.schema._public.services.platform.CheckInReturnDocument;
import com.docuware.dev.schema._public.services.platform.DocumentActionParameters;
import com.docuware.dev.schema._public.services.platform.DocumentVersion;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"DocumentVersion", "CheckInReturnDocument"})
public class DocuWareCheckInActionParameters extends DocumentActionParameters {

	@JsonProperty("DocumentVersion")
	protected DocuWareDocumentVersion documentVersion;
	@JsonProperty("CheckInReturnDocument")
	@JsonSerialize(using = CheckInReturnDocumentSerializer.class)
	protected CheckInReturnDocument checkInReturnDocument;
	@JsonProperty("Comments")
	protected String comments;

	/**
	 * Ruft den Wert der documentVersion-Eigenschaft ab.
	 * 
	 * @return
	 *     possible object is
	 *     {@link DocumentVersion }
	 *     
	 */
	public DocuWareDocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	/**
	 * Legt den Wert der documentVersion-Eigenschaft fest.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link DocumentVersion }
	 *     
	 */
	public void setDocumentVersion(DocuWareDocumentVersion value) {
		this.documentVersion = value;
	}

	/**
	 * Ruft den Wert der checkInReturnDocument-Eigenschaft ab.
	 * 
	 * @return
	 *     possible object is
	 *     {@link CheckInReturnDocument }
	 *     
	 */
	public CheckInReturnDocument getCheckInReturnDocument() {
		return checkInReturnDocument;
	}

	/**
	 * Legt den Wert der checkInReturnDocument-Eigenschaft fest.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link CheckInReturnDocument }
	 *     
	 */
	public void setCheckInReturnDocument(CheckInReturnDocument value) {
		this.checkInReturnDocument = value;
	}

	/**
	 * Ruft den Wert der comments-Eigenschaft ab.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * Legt den Wert der comments-Eigenschaft fest.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public void setComments(String value) {
		this.comments = value;
	}

	public static class CheckInReturnDocumentSerializer extends JsonSerializer<CheckInReturnDocument> {
		@Override
		public void serialize(CheckInReturnDocument value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if(value != null) {
				gen.writeString(value.value());
			}
			else {
				gen.writeNull();
			}
		}
	}
}
