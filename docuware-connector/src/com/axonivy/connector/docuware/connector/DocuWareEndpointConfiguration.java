package com.axonivy.connector.docuware.connector;

public class DocuWareEndpointConfiguration {

	private String fileCabinetId;
				
	public String getFileCabinetId() {
		return fileCabinetId;
	}

	public void setFileCabinetId(String fileCabinetId) {
		this.fileCabinetId = fileCabinetId;
	}

	@Override
	public String toString() {
		return String.format("filecabinet: %s ", fileCabinetId);
	}

}
