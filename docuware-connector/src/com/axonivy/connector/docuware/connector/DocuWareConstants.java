package com.axonivy.connector.docuware.connector;

import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;

public class DocuWareConstants {

  public static final String ERROR_BASE = "com:eon:ivy:shared:docuware:rest";
  public static final String MESSAGE_TAG3 = "msg";
  public static final String PROPERTY_DISPLAYNAME3 = "displayname";
  public static final String PROPERTY_CONTENT_TYPE3 = "contentType";
  public static final String RESPONSE_STATUS_CODE_ATTRIBUTE = "RestClientResponseStatusCode";
  public static final String SEMICOLON = ";";
  public static final String ARCHIVED_INSTANCE = "archivedInstance";
  public static final String INSTANCES = "instances";
  public static final String INSTANCE_PATTERN = DocuWareVariable.ROOT.variableKey + "." + INSTANCES + ".";
  public static final String INSTANCE_PROPERTY_PATTERN = INSTANCE_PATTERN + "%s.%s";
  public static final String VIEW_DOCUMENT_URL_FORMAT = "https://%s/DocuWare/Platform/WebClient/Client/Document?did=%s&fc=%s";
}
