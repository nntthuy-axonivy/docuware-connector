package com.axonivy.connector.docuware.connector.enums;

import java.util.stream.Stream;

import ch.ivyteam.ivy.environment.Ivy;

public enum DocuWareVariable {
  ROOT("docuwareConnector"),
  HOST("host"),
  PLATFORM("platform"),
  USERNAME("username"),
  PASSWORD("password"),
  ACCESS_TOKEN("accessToken"),
  GRANT_TYPE("grantType"),
  TRUSTED_USERNAME("trustedUserName"),
  TRUSTED_USER_PASSWORD("trustedUserPassword"),
  LOGIN_TOKEN("loginToken"),
  DEFAULT_INSTANCE("defaultInstance"),
  FILE_CABINET_ID("fileCabinetId"),
  STORE_DIALOG_ID("storedialogid"),
  CONNECT_TIMEOUT("connectTimeout"),
  @Deprecated
  HOST_ID("hostid"),
  @Deprecated
  LOGON_URL("logonurl");

  public String variableKey;
  public static final String SEPERATOR = ".";

  private DocuWareVariable(String variableKey) {
    this.variableKey = variableKey;
  }

  public String getVariableName() {
    return ROOT.variableKey + SEPERATOR + variableKey;
  }

  public String getValue() {
    return Ivy.var().get(getVariableName());
  }
  
  public String getValueForInstance(String instanceName) {
    return Ivy.var().get(ROOT.variableKey + SEPERATOR + instanceName + SEPERATOR + variableKey);
  }

  public String updateValue(String newValue) {
    return Ivy.var().set(getVariableName(), newValue);
  }

  public static DocuWareVariable of(String variableName) {
    return Stream.of(values()).filter(var -> var.variableKey.equals(variableName)).findAny().orElse(null);
  }
}
