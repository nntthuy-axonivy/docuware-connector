package com.axonivy.connector.docuware.connector.auth.oauth;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.axonivy.connector.docuware.connector.enums.GrantType;

import ch.ivyteam.ivy.environment.Ivy;

public class GlobalVarConfiguration extends Configuration {
	/**
	 * Base of all DocueWare connector variables.
	 */
	public static final String DOCUWARE_CONNECTOR_VAR = "docuwareConnector";

	public GlobalVarConfiguration(String key) {
		super(key);
		refresh();
	}

	@Override
	public void refresh() {
		var configKey = getConfigKey();
		setConfigId(getConfigId(configKey));
		setInherit(getConfigVar(configKey, DocuWareVariable.INHERIT, null));
		setUrl(getConfigVar(configKey, DocuWareVariable.URL, null));
		setGrantType(GrantType.of(getConfigVar(configKey, DocuWareVariable.GRANT_TYPE, null)));
		setUsername(getConfigVar(configKey, DocuWareVariable.USERNAME, null));
		setPassword(getConfigVar(configKey, DocuWareVariable.PASSWORD, null));
		setImpersonateStrategy(ImpersonateStrategy.create(getConfigVar(configKey, DocuWareVariable.IMPERSONATE_USER, null)));
		setDwTokenStrategy(DwTokenStrategy.create(getConfigVar(configKey, DocuWareVariable.DW_TOKEN, null)));
		setIntegrationPassphrase(getConfigVar(configKey, DocuWareVariable.INTEGRATION_PASSPHRASE, null));
		setConnectTimeout(getConfigVarAsLong(configKey, DocuWareVariable.CONNECT_TIMEOUT, null));
		setReadTimeout(getConfigVarAsLong(configKey, DocuWareVariable.READ_TIMEOUT, null));
		setLoggingEntityMaxSize(getConfigVarAsLong(configKey, DocuWareVariable.LOGGING_ENTITY_MAX_SIZE, null));

		super.refresh();
	}

	/**
	 * Get a variable with absolute path.
	 * 
	 * @param name absolute path
	 * @param def default value
	 * @return
	 */
	public static String getVar(String name, String def) {
		return Optional.ofNullable(Ivy.var().get(name)).orElse(def);
	}

	/**
	 * Get the id of a configuration.
	 * 
	 * <p>
	 * The id of a configuration is only used to determine, whether a configuration has changed
	 * (and therefore cached values must be re-read). You can put there any value. A timestamp
	 * and a user name might be informative.
	 * </p>
	 * @param configKey
	 * @return
	 */
	public static String getConfigId(String configKey) {
		return getConfigVar(configKey, DocuWareVariable.CONFIG_ID, null);
	}


	/**
	 * Get a variable in the Docuware Namespace and handle inheritance.
	 * 
	 * <p>
	 * Convenience function to use predefined variable names.
	 * </p>
	 * 
	 * @param configKey
	 * @param variable variable inside config
	 * @param def default value
	 * @return
	 */
	public static String getConfigVar(String configKey, DocuWareVariable variable, String def) {
		return getConfigVar(configKey, variable.varName(), def);
	}

	/**
	 * Get a variable in the Docuware Namespace and handle inheritance.
	 * 
	 * <p>
	 * Convenience function to use predefined variable names.
	 * </p>
	 * 
	 * @param configKey
	 * @param variable variable inside config
	 * @param def default value
	 * @return
	 */
	public static Long getConfigVarAsLong(String configKey, DocuWareVariable variable, Long def) {
		Long result = def;
		var val = getConfigVar(configKey, variable.varName(), null);
		if(StringUtils.isNotBlank(val)) {
			result = Long.valueOf(val);
		}
		return result;
	}

	/**
	 * Get a variable in the Docuware Namespace.
	 * 
	 * @param configKey
	 * @param name name inside config
	 * @param def default value
	 * @return
	 */
	public static String getConfigVar(String configKey, String name, String def) {
		return getVar("%s.%s.%s".formatted(DOCUWARE_CONNECTOR_VAR, configKey, name), def);
	}

	/**
	 * Get the config id of a configuration.
	 * 
	 * @param configKey
	 * @param def
	 * @return
	 */
	public String getConfigId(String configKey, String def) {
		return getConfigVar(configKey, DocuWareVariable.CONFIG_ID, def);
	}

	/**
	 * Check if the configId is up to date.
	 * 
	 * @param key
	 * @param configId
	 * @return
	 */
	@Override
	public boolean isValidConfigId() {
		return StringUtils.equals(getConfigId(getConfigKey()), getConfigId());
	}
}
