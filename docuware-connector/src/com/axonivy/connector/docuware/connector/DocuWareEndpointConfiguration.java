package com.axonivy.connector.docuware.connector;

import java.util.Arrays;
import java.util.List;

import com.axonivy.connector.docuware.connector.enums.GrantType;

public class DocuWareEndpointConfiguration {
  private String instance;
  private String host;
  private GrantType grantType;
  private String username;
  private String password;
  private String organization;
  private String fileCabinetId;
  private List<String> fileCabinetIds;
  private String storeDialogId;
  private Integer connectTimeout;
  private String trustedUserName;
  private String trustedUserPassword;
  private String accessToken;
  private String loginToken;

  public DocuWareEndpointConfiguration() { }
  
  public DocuWareEndpointConfiguration(String instance) {
    this.instance = instance;
  }

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public GrantType getGrantType() {
    return grantType;
  }

  public void setGrantType(GrantType grantType) {
    this.grantType = grantType;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getFileCabinetId() {
    return fileCabinetId;
  }

  public void setFileCabinetId(String fileCabinetId) {
    this.fileCabinetId = fileCabinetId;
  }

  public List<String> getFileCabinetIds() {
    return fileCabinetIds;
  }

  public void setFileCabinetIds(List<String> fileCabinetIds) {
    this.fileCabinetIds = fileCabinetIds;
  }

  public String getStoreDialogId() {
    return storeDialogId;
  }

  public void setStoreDialogId(String storeDialogId) {
    this.storeDialogId = storeDialogId;
  }

  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(Integer connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public String getTrustedUserName() {
    return trustedUserName;
  }

  public void setTrustedUserName(String trustedUserName) {
    this.trustedUserName = trustedUserName;
  }

  public String getTrustedUserPassword() {
    return trustedUserPassword;
  }

  public void setTrustedUserPassword(String trustedUserPassword) {
    this.trustedUserPassword = trustedUserPassword;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getLoginToken() {
    return loginToken;
  }

  public void setLoginToken(String loginToken) {
    this.loginToken = loginToken;
  }

  @Override
  public String toString() {
    return String.format("instance: %s / host: %s / username: %s / organization: %s / filecabinet: %s / storeDialogId: %s / connectTimeout: %d",
        instance, host, username, organization, Arrays.asList(fileCabinetId, fileCabinetIds), storeDialogId, connectTimeout);
  }

}
