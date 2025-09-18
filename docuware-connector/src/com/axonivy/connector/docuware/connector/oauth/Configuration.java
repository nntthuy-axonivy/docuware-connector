package com.axonivy.connector.docuware.connector.oauth;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;

import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.enums.GrantType;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.ISecurityConstants;

/**
 * Configuration of a DocuWare connection.
 */
public abstract class Configuration {
	/**
	 * Name of the default configuration.
	 */
	protected static final String DOCUWARE_DEFAULT_CONFIG = "defaultConfig";
	public static final String APP_ATT_CONFIG_PREFIX = Configuration.class.getCanonicalName();
	protected static final String DOCUWARE_USERNAME = "%s:%s".formatted(DocuWareService.class.getCanonicalName(), "dwusername");
	protected static final String DOCUWARE_TOKEN = "%s:%s".formatted(DocuWareService.class.getCanonicalName(), "dwtoken");
	protected static final String CONFIG_ERROR = DocuWareService.DOCUWARE_ERROR + "configuration:";
	private String configKey;
	private String configId;
	private String inherit;
	private String url;
	private GrantType grantType;
	private String username;
	private String password;
	private ImpersonateStrategy impersonateStrategy;
	private DwTokenStrategy dwTokenStrategy;
	private String integrationPassphrase;
	private Long connectTimeout;
	private Long readTimeout;
	private Long loggingEntityMaxSize;
	// Determined lazy by REST calls to DW.
	private String tokenEndpoint;

	public Configuration(String configKey) {
		if(configKey == null) {
			BpmError.create(DocuWareService.DOCUWARE_ERROR + "missingKey")
			.withMessage("Key is missing for configuration.")
			.throwError();
		}
		setConfigKey(configKey);
		putKnownConfiguration(this);
	}

	public static void putKnownConfiguration(Configuration configuration) {
		var key = configuration.getConfigKey();
		IApplication.current().setAttribute(createConfigurationCacheKey(key), configuration);
	}

	public static Configuration getKnownConfiguration(String configKey) {
		Configuration configuration = null;
		try {
			configuration = (Configuration)IApplication.current().getAttribute(createConfigurationCacheKey(configKey));
		} catch (ClassCastException e) {
			Ivy.log().error("Cache contained an old version of the configuration class, ignoring it.");
		}
		if(configuration == null) {
			// Currently, new configurations can only be created from global variables.
			configuration = new GlobalVarConfiguration(configKey);
			putKnownConfiguration(configuration);
		}
		else if(!configuration.isValidConfigId()) {
			// All types of configuration must know how to refresh.
			configuration.refresh();
		}
		return configuration;
	}

	/**
	 * Get a configuration, if key is empty, return default configuration.
	 * 
	 * @param configKey
	 * @return
	 */
	public static Configuration getKnownConfigurationOrDefault(String configKey) {
		return getKnownConfiguration(knownOrDefaultKey(configKey));
	}

	/**
	 * Return key and if empty return default configuration key.
	 * 
	 * @param configKey
	 * @return
	 */
	public static String knownOrDefaultKey(String configKey) {
		return StringUtils.isBlank(configKey) ? DOCUWARE_DEFAULT_CONFIG : configKey;
	}

	protected static String createConfigurationCacheKey(String configKey) {
		return "%s:%s".formatted(APP_ATT_CONFIG_PREFIX, configKey);
	}

	/**
	 * Compare the stored config id to the live value.
	 * 
	 * @return
	 */
	public abstract boolean isValidConfigId();

	/**
	 * Re-read the whole configuration.
	 * 
	 * Override this function and call the super function at the end to
	 * make sure, that outdated stuff is cleaned and data i roughly validated.
	 */
	public void refresh() {
		tokenEndpoint = null;

		if(StringUtil.isBlank(url)) {
			BpmError.create(CONFIG_ERROR + "nourl")
			.withMessage("The url is not set for config '%s'".formatted(configKey))
			.throwError();
		}
		if(grantType == null) {
			BpmError.create(CONFIG_ERROR + "nogranttype")
			.withMessage("The grantType is not set for config '%s'".formatted(configKey))
			.throwError();
		}
	}

	/**
	 * Get a value with inheritance.
	 * 
	 * @param <T>
	 * @param getter
	 * @return
	 */
	protected <T> T get(Function<Configuration, T> getter) {
		return internalGet(getter, new HashSet<String>());
	}

	protected <T> T internalGet(Function<Configuration, T> getter, Set<String> seen) {
		var val = getter.apply(this);
		if((val == null || StringUtils.isBlank(val.toString())) && StringUtils.isNotBlank(inherit)) {
			if(seen.add(inherit)) {
				var cfg = getKnownConfiguration(inherit);
				if(cfg != null) {
					val = cfg.internalGet(getter, seen);
				}
			}
			else {
				DocuWareService.get().clearCaches();
				BpmError
				.create(DocuWareService.DOCUWARE_ERROR + "invalidconfiguration")
				.withMessage("Found inheritance loop for key '%s'. Clearing all caches.".formatted(inherit))
				.throwError();
			}
		}
		return val;
	}

	public String getConfigKey() {
		return configKey;
	}
	public void setConfigKey(String key) {
		this.configKey = key;
	}
	public String getConfigId() {
		return get(c -> c.configId);
	}
	public void setConfigId(String configId) {
		this.configId = configId;
	}
	public String getInherit() {
		return inherit;
	}
	public void setInherit(String inherit) {
		this.inherit = inherit;
	}
	public String getUrl() {
		return get(c -> c.url);
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public GrantType getGrantType() {
		return get(c -> c.grantType);
	}
	public void setGrantType(GrantType grantType) {
		this.grantType = grantType;
	}
	public String getUsername() {
		return get(c -> c.username);
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return get(c -> c.password);
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public ImpersonateStrategy getImpersonateStrategy() {
		return get(c -> c.impersonateStrategy);
	}
	public void setImpersonateStrategy(ImpersonateStrategy impersonateStrategy) {
		this.impersonateStrategy = impersonateStrategy;
	}
	public DwTokenStrategy getDwTokenStrategy() {
		return get(c -> c.dwTokenStrategy);
	}
	public void setDwTokenStrategy(DwTokenStrategy dwTokenStrategy) {
		this.dwTokenStrategy = dwTokenStrategy;
	}
	public String getIntegrationPassphrase() {
		return get(c -> c.integrationPassphrase);
	}
	public void setIntegrationPassphrase(String integrationPassphrase) {
		this.integrationPassphrase = integrationPassphrase;
	}
	public Long getConnectTimeout() {
		return get(c -> c.connectTimeout);
	}
	public void setConnectTimeout(Long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	public Long getReadTimeout() {
		return get(c -> c.readTimeout);
	}
	public void setReadTimeout(Long readTimeout) {
		this.readTimeout = readTimeout;
	}
	public Long getLoggingEntityMaxSize() {
		return get(c -> c.loggingEntityMaxSize);
	}
	public void setLoggingEntityMaxSize(Long loggingEntityMaxSize) {
		this.loggingEntityMaxSize = loggingEntityMaxSize;
	}
	public String getTokenEndpoint() {
		return tokenEndpoint;
	}
	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}
	public boolean hasTokenEndpoint() {
		return tokenEndpoint != null;
	}

	/**
	 * Get the DW user name based on the strategy. 
	 * 
	 * @return
	 */
	public String getImpersonateUserName() {
		String result = null;
		var session = Ivy.session();

		switch(impersonateStrategy.getStrategy()) {
		case CONSTANT:
			result = impersonateStrategy.getSystemUser();
			break;
		case FIXED:
			if(session.isSessionUserSystemUser() || ISecurityConstants.DEVELOPER_USER_NAME.equals(session.getSessionUserName())) {
				result = impersonateStrategy.getSystemUser();
			}
			else if(session.isSessionUserUnknown()) {
				result = impersonateStrategy.getAnonymousUser();
			}
			else {
				result = impersonateStrategy.getIvyUser();
			}
			break;
		case IVY:
			if(session.isSessionUserSystemUser() || ISecurityConstants.DEVELOPER_USER_NAME.equals(session.getSessionUserName())) {
				result = impersonateStrategy.getSystemUser();
			}
			else if(session.isSessionUserUnknown()) {
				result = impersonateStrategy.getAnonymousUser();
			}
			else {
				result = session.getSessionUserName();
			}
			break;
		case SESSION:
			result = getSessionDocuwareUser();
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * Get the DW token based on the strategy. 
	 * 
	 * @return
	 */
	public String getDwToken() {
		String result = null;

		switch(dwTokenStrategy.getStrategy()) {
		case SESSION:
			result = getSessionDocuwareToken();
			break;
		default:
			break;
		}
		return result;
	}


	/**
	 * Get a username stored in session.
	 * 
	 * <p>
	 * Use to impersonate user of type session.
	 * </p>
	 * 
	 * @return
	 */
	public String getSessionDocuwareUser() {
		return (String) Ivy.session().getAttribute(docuWareUserKey());
	}

	/**
	 * Set a username stored in session.
	 * 
	 * <p>
	 * Use to impersonate user of type session.
	 * </p>
	 * 
	 * @param username
	 */
	public void setSessionDocuwareUser(String username) {
		Ivy.session().setAttribute(docuWareUserKey(), username);
	}

	/**
	 * Get a dw token stored in session.
	 * 
	 * <p>
	 * Use for dw token of type session.
	 * </p>
	 * 
	 * @return
	 */
	public String getSessionDocuwareToken() {
		return (String) Ivy.session().getAttribute(docuWareTokenKey());
	}

	/**
	 * Set a dw token stored in session.
	 * 
	 * <p>
	 * Use for dw token of type session.
	 * </p>
	 * 
	 * @param token
	 */
	public void setSessionDocuwareToken(String token) {
		Ivy.session().setAttribute(docuWareTokenKey(), token);
	}

	public String docuWareUserKey() {
		return "%s:%s".formatted(DOCUWARE_USERNAME, configKey);
	}

	public String docuWareTokenKey() {
		return "%s:%s".formatted(DOCUWARE_TOKEN, configKey);
	}

	@Override
	public String toString() {
		return String.format(
				"Configuration [key=%s, configId=%s, inherit=%s, url=%s, grantType=%s, username=%s, password=%s, impersonateStrategy=%s, dwTokenStrategy=%s, integrationPassphrase=%s, connectTimeout=%s, readTimeout=%s, loggingEntityMaxSize=%s, tokenEndpoint=%s]",
				getConfigKey(), getConfigId(), getInherit(), getUrl(), getGrantType(), getUsername(), safeShow(getPassword()), getImpersonateStrategy(), getDwTokenStrategy(),
				safeShow(getIntegrationPassphrase()), getConnectTimeout(), getReadTimeout(), getLoggingEntityMaxSize(), getTokenEndpoint());
	}

	private String safeShow(String sensitive) {
		return sensitive == null ? null : sensitive.replaceAll(".", "*");
	}


}
