package com.axonivy.connector.docuware.connector.enums;

public enum DocuWareVariable {
	/**
	 * Name of the property that will be used to determine whether a cached connection needs to be renewed.
	 */
	CONFIG_ID("configId"),
	/**
	 * Name of the property that contains the name of a config to inherit from in case a value is not set.
	 */
	INHERIT("inherit"),
	URL("url"),
	GRANT_TYPE("grantType"),
	USERNAME("username"),
	PASSWORD("password", true),
	IMPERSONATE_USER("impersonateUser"),
	DW_TOKEN("dwToken"),
	INTEGRATION_PASSPHRASE("integrationPassphrase", true),
	CONNECT_TIMEOUT("connectTimeout"),
	READ_TIMEOUT("readTimeout"),
	LOGGING_ENTITY_MAX_SIZE("loggingEntityMaxSize"),
	;

	private String varName;
	private boolean secret = false;
	private boolean obsolete = false;

	private DocuWareVariable(String varName) {
		this.varName = varName;
	}

	private DocuWareVariable(String varName, boolean secret) {
		this.varName = varName;
		this.secret = secret;
	}

	private DocuWareVariable(String varName, boolean secret, boolean obsolete) {
		this.varName = varName;
		this.secret = secret;
		this.obsolete = obsolete;
	}

	public String varName() {
		return varName;
	}

	public boolean isSecret() {
		return secret;
	}

	public boolean isObsolete() {
		return obsolete;
	}
}
