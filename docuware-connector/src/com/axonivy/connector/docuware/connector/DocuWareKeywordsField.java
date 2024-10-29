package com.axonivy.connector.docuware.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DocuWareKeywordsField class to match the JSON format used by the Docuware rest
 * services for setting item value of {@link DocuWareProperty} which
 * type="Keywords".
 */
@JsonPropertyOrder({ "$type", "Keyword" })
public class DocuWareKeywordsField {

	@JsonProperty(value = "$type")
	final private String type = "DocumentIndexFieldKeywords";

	@JsonProperty(value = "Keyword")
	private List<String> keywords;

	public DocuWareKeywordsField() {
		keywords = new ArrayList<>();
	}

	/**
	 * @return the keywords
	 */
	public List<String> getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @param value
	 * Utility function to add keyword to the list
	 */
	public void append(String value) {
		if (Objects.isNull(keywords)) {
			keywords = new ArrayList<>();
		}
		keywords.add(value);
	}

}
