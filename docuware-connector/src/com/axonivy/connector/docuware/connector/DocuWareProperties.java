package com.axonivy.connector.docuware.connector;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties class to match the JSON format used by the Docuware rest services for setting index fields during a file upload
 *
 */
public class DocuWareProperties {
	@JsonProperty(value = "Fields")
	private List<DocuWareProperty> properties;

	public DocuWareProperties() {
		initialize(true);
	}
	
	public DocuWareProperties(List<DocuWareProperty> properties) {
		this.properties = properties;
	}
	
	
	private void initialize(boolean initiateContentProperties) {
		properties = new ArrayList<>();
	}

	public List<DocuWareProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<DocuWareProperty> properties) {
		this.properties = properties;
	}


	public void addProperty(String fieldName, Object item, String itemElementName) {
		properties.add(new DocuWareProperty(fieldName, item, itemElementName));
	}

}
