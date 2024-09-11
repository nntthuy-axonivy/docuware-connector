package com.axonivy.connector.docuware.connector;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DocumentIndexFieldTable class to match the JSON format used by the Docuware rest services
 * for setting item value of {@link DocuWareProperty} which type="Table".
 * 
 * It support a inner builder for table row and column values.<br/>
 * Example:<br/>
 * <pre>
 * DocuWareFieldTableItem tableItem = new DocuWareFieldTableItem();
 * tableItem.createRow()
 *	  .addColumnValue("tableColumnField1", "value1", "String")
 *	  .addColumnValue("tableColumnField2", "value2", "String")
 *	  .addColumnValue("tableColumnField3", "3.00", "String");
 * </pre> 
 * Then use the object for {@link DocuWareProperty} :
 * <pre>DocuWareProperty indexFieldTable = new DocuWareProperty("IndexTableFieldName1", tableItem, "Table");</pre>
 */
@JsonPropertyOrder({"$type", "Row"})
public class DocuWareFieldTableItem {

  @JsonProperty(value = "$type")
  final private String type = "DocumentIndexFieldTable";

  @JsonProperty(value = "Row")
  private List<DocuWareTableRow> rows;
  
  public DocuWareFieldTableItem() {
    initialize(true);
  }

  public DocuWareFieldTableItem(List<DocuWareTableRow> rows) {
    this.rows = rows;
  }

  private void initialize(boolean initiateContentProperties) {
    rows = new ArrayList<>();
  }

  public List<DocuWareTableRow> getRows() {
    return rows;
  }

  public void setRows(List<DocuWareTableRow> rows) {
    this.rows = rows;
  }

  public DocuWareTableRow createRow() {
    DocuWareTableRow row = new DocuWareTableRow();
    this.rows.add(row);
    return row;
  }
  
  public class DocuWareTableRow {
    @JsonProperty(value = "ColumnValue")
    private List<DocuWareProperty> columnValues;
	
    public DocuWareTableRow() {
      initialize(true);
    }

    public DocuWareTableRow(List<DocuWareProperty> columnValues) {
      this.columnValues = columnValues;
    }

    private void initialize(boolean initiateContentProperties) {
      columnValues = new ArrayList<>();
    }
	
    public DocuWareTableRow addColumnValue(String fieldName, Object item, String itemElementName) {
      columnValues.add(new DocuWareProperty(fieldName, item, itemElementName));
      return this;
    }
	
    public List<DocuWareProperty> getColumnValues() {
      return columnValues;
    }

    public void setColumnValues(List<DocuWareProperty> columnValues) {
      this.columnValues = columnValues;
    }
  }
}
