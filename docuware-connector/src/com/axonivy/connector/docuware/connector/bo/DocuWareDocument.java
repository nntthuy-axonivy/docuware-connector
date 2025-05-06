package com.axonivy.connector.docuware.connector.bo;

import com.docuware.dev.schema._public.services.platform.Document;

public class DocuWareDocument {
  private Integer id;
  private String fileCabinetId;
  private Document document;

  public DocuWareDocument() {
    super();
  }

  public DocuWareDocument(String fileCabinetId, Document document) {
    super();
    this.fileCabinetId = fileCabinetId;
    this.document = document;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFileCabinetId() {
    return fileCabinetId;
  }

  public void setFileCabinetId(String fileCabinetId) {
    this.fileCabinetId = fileCabinetId;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

}
