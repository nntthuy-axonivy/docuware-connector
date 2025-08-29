package com.axonivy.connector.docuware.connector.auth.oauth;

import com.axonivy.connector.docuware.connector.DocuWareService;

/**
 * Configuration properties to cache.
 */
public class Configuration {
	private String config;
	private String configId;
	private String tokenEndpoint;
	private ImpersonateStrategy impersonateStrategy;

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

	public String getImpersonateUserName() {
		if(impersonateStrategy == null) {
			impersonateStrategy = DocuWareService.get().getImpersonateStrategy(config);
		}
		return impersonateStrategy.getImpersonateUserName();
	}

	@Override
	public String toString() {
		return "Configuration [config=%s, configId=%s, tokenEndpoint=%s]".formatted(config, configId, tokenEndpoint);
	}

}
