package com.axonivy.connector.docuware.connector.auth.oauth;

import com.axonivy.connector.docuware.connector.enums.GrantType;

/**
 * Configuration properties to cache.
 */
public class Configuration {
	private String config;
	private String configId;
	private String tokenEndpoint;
	private ImpersonateStrategy impersonateStrategy;
	private DwTokenStrategy dwTokenStrategy;
	private GrantType grantType;

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}

	public ImpersonateStrategy getImpersonateStrategy() {
		return impersonateStrategy;
	}

	public void setImpersonateStrategy(ImpersonateStrategy impersonateStrategy) {
		this.impersonateStrategy = impersonateStrategy;
	}

	public DwTokenStrategy getDwTokenStrategy() {
		return dwTokenStrategy;
	}

	public void setDwTokenStrategy(DwTokenStrategy dwTokenStrategy) {
		this.dwTokenStrategy = dwTokenStrategy;
	}

	public void setGrantType(GrantType grantType) {
		this.grantType = grantType;
	}

	public GrantType getGrantType() {
		return grantType;
	}
}
