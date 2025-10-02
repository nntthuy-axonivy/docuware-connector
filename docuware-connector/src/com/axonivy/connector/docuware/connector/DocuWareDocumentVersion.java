package com.axonivy.connector.docuware.connector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"Major", "Minor"})
public class DocuWareDocumentVersion {

	@JsonProperty("Major")
	protected int major;
	@JsonProperty("Minor")
	protected int minor;

	/**
	 * Ruft den Wert der major-Eigenschaft ab.
	 * 
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Legt den Wert der major-Eigenschaft fest.
	 * 
	 */
	public void setMajor(int value) {
		this.major = value;
	}

	/**
	 * Ruft den Wert der minor-Eigenschaft ab.
	 * 
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Legt den Wert der minor-Eigenschaft fest.
	 * 
	 */
	public void setMinor(int value) {
		this.minor = value;
	}

}
