package com.axonivy.connector.docuware.connector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Helper class used in {@link DocuWareProperties} to map the JSON used in
 * Docuware rest services.
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"FieldName", "Item", "ItemElementName", "ReadOnly"})
public class DocuWareProperty {

  @JsonProperty("FieldName")
  private String fieldName;
  @JsonProperty("Item")
  private Object item;
  @JsonProperty("ItemElementName")
  private Object itemElementName;
  @JsonProperty("ReadOnly")
  private Boolean isReadOnly;

  public DocuWareProperty() {}

  public DocuWareProperty(String fieldName, Object item, String itemElementName) {
    this.fieldName = fieldName;
    this.item = item;
    this.itemElementName = itemElementName;
  }

  public DocuWareProperty(String fieldName, Object item, String itemElementName, Boolean isReadOnly) {
    this.fieldName = fieldName;
    this.item = item;
    this.itemElementName = itemElementName;
    this.isReadOnly = isReadOnly;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Object getItem() {
    return item;
  }

  public void setItem(Object item) {
    this.item = item;
  }

  public Object getItemElementName() {
    return itemElementName;
  }

  public void setItemElementName(Object itemElementName) {
    this.itemElementName = itemElementName;
  }

  public Boolean isReadOnly() {
    return isReadOnly;
  }

  public void setReadOnly(Boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
  }
}
