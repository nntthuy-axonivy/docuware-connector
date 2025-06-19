package com.axonivy.connector.docuware.connector;

public class DocuWareEndpointConfiguration {

  private String fileCabinetId;
  private String storeDialogId;

  public String getFileCabinetId() {
    return fileCabinetId;
  }

  public void setFileCabinetId(String fileCabinetId) {
    this.fileCabinetId = fileCabinetId;
  }
  
  public String getStoreDialogId() {
	return storeDialogId;
  }
  
  public void setStoreDialogId(String storeDialogId) {
	this.storeDialogId = storeDialogId;
  }

  @Override
  public String toString() {
    return String.format("filecabinet: %s / storeDialogId: %s", fileCabinetId, storeDialogId);
  }
}
