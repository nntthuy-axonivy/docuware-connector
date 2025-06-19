package com.axonivy.connector.docuware.connector.enums;

import ch.ivyteam.ivy.environment.Ivy;

public enum DocuWareVariable {
	SCHEME("docuwareConnector.scheme"),
	HOST("docuwareConnector.host"),
	PLATFORM("docuwareConnector.platform"),
	USERNAME("docuwareConnector.username"),
	PASSWORD("docuwareConnector.password"),
	GRANT_TYPE("docuwareConnector.grantType"),
	TRUSTED_USERNAME("docuwareConnector.trustedUserName"),
	TRUSTED_USER_PASSWORD("docuwareConnector.trustedUserPassword"),
	LOGIN_TOKEN("docuwareConnector.loginToken"),
	INTEGRATION_PASSPHRASE("docuwareConnector.integrationPassphrase");

	private String variableName;

	private DocuWareVariable(String variableName) {
		this.variableName = variableName;
	}

	public String getVariableName() {
		return variableName;
	}

	public String getValue() {
		return Ivy.var().get(variableName);
	}

	public String updateValue(String newValue) {
		return Ivy.var().set(variableName, newValue);
	}
}
