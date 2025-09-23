package com.axonivy.connector.docuware.connector.oauth;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.docuware.connector.DocuWareService;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.bpm.error.BpmPublicErrorBuilder;
import ch.ivyteam.ivy.environment.Ivy;

public class DocuWareAuthFeature implements Feature {
	private static final String WELL_KNOWN_OPENID_CONFIGURATION = ".well-known/openid-configuration";
	private static final String IDENTITY_SERVICE_INFO_URL = "Home/IdentityServiceInfo";
	private static final String IDENTITY_SERVICE_URL = "IdentityServiceUrl";
	private static final String TOKEN_ENDPOINT_URL = "token_endpoint";
	private static final String SCOPE = "docuware.platform";
	private static final String CLIENT_ID = "docuware.platform.net.client";
	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer ";
	public static final String SKIP_FILTER = "Skip-%s".formatted(DocuWareBearerFilter.class.getCanonicalName());
	public static final String URI_PLACEHOLDER = "URI.PLACEHOLDER";

	@Override
	public boolean configure(FeatureContext context) {
		var bearerFilter = new DocuWareBearerFilter();
		context.register(bearerFilter, Priorities.AUTHORIZATION);
		return true;
	}


	public class DocuWareBearerFilter implements ClientRequestFilter {

		/**
		 * Make sure, that we have a token in the Bearer header.
		 */
		@Override
		public void filter(ClientRequestContext context) throws IOException {
			var skip = context.getProperty(SKIP_FILTER) == Boolean.TRUE;

			// If the request is coming from the feature itself, then the configuration
			// does not contain the configKey but it must be fetched from the request context directly.
			var configKey = skip ? getContextKey(context) : getConfigKey(context);

			var cfg = Configuration.getKnownConfigurationOrDefault(configKey);

			context.setProperty("jersey.config.client.connectTimeout", cfg.getConnectTimeout());
			context.setProperty("jersey.config.client.readTimeout", cfg.getReadTimeout());
			context.setProperty("jersey.config.client.logging.entity.maxSize", cfg.getLoggingEntityMaxSize());

			if(skip) {
				Ivy.log().debug("Request filter is skipped for ''{0}''.", context.getUri());
			}
			else {
				var accessToken = getAccessToken(context);
				context.getHeaders().add(AUTHORIZATION, BEARER + accessToken);
				var uri = context.getUri().toString();
				context.setUri(URI.create(uri.replace(URI_PLACEHOLDER, cfg.getUrl())));
				Ivy.log().debug("Changed Uri ''{0}'' to ''{1}''", uri, context.getUri());
			}
		}
	}

	/**
	 * Get the cached token or a new one.
	 * 
	 * @param context
	 * @return
	 */
	private String getAccessToken(ClientRequestContext context) {
		var configKey = getConfigKey(context);

		var cfg = Configuration.getKnownConfigurationOrDefault(configKey);

		var token = DocuWareService.get().getCachedToken(cfg);

		if (token == null || token.isExpired() || !token.getConfigId().equals(cfg.getConfigId())) {
			token = fetchAccessToken(context);
			DocuWareService.get().setCachedToken(cfg, token);
			Ivy.log().debug("Cached a new token: {0}", token);
		}

		if (!token.hasAccessToken()) {
			DocuWareService.get().setCachedToken(cfg, null);
			authError("accesstoken")
			.withMessage("Failed to get access token for config '%s' and token %s".formatted(configKey, token))
			.throwError();
		}

		return token.accessToken();
	}

	/**
	 * Fetch a new token.
	 * 
	 * @param client
	 * @param config
	 * @return
	 */
	private Token fetchAccessToken(ClientRequestContext context) {
		var configKey = getConfigKey(context);

		var cfg = Configuration.getKnownConfigurationOrDefault(configKey);

		if(!cfg.hasTokenEndpoint()) {
			cfg.setTokenEndpoint(fetchEndpointUrl(context));
		}

		Ivy.log().debug("Fetching token from url ''{0}''", cfg.getTokenEndpoint());

		var grantType = cfg.getGrantType();
		var payload = new MultivaluedHashMap<String, String>();

		payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_GRANT_TYPE, List.of(grantType.code()));
		payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_SCOPE, List.of(SCOPE));
		payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_CLIENT_ID, List.of(CLIENT_ID));

		switch(grantType) {
		case PASSWORD:
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_USERNAME, List.of(cfg.getUsername()));
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_PASSWORD, List.of(cfg.getPassword()));
			break;
		case TRUSTED:
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_USERNAME, List.of(cfg.getUsername()));
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_PASSWORD, List.of(cfg.getPassword()));
			var impersonateUserName = cfg.getImpersonateUserName();
			if(StringUtils.isBlank(impersonateUserName)) {
				authError("fetchtoken:missingimpersonateuser")
				.withMessage("Cannot fetch access token for config '%s' and grant type '%s' because the impersonate user is not set.".formatted(configKey, grantType.code()))
				.throwError();
			}
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_IMPERSONATE_NAME, List.of(impersonateUserName));
			break;
		case DW_TOKEN:
			String dwToken = cfg.getDwToken();
			if(StringUtils.isBlank(dwToken)) {
				authError("fetchtoken:missingdwtoken")
				.withMessage("Cannot fetch access token for config '%s' and grant type '%s' because the dw token is not set.".formatted(configKey, grantType.code()))
				.throwError();
			}
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_TOKEN, List.of(dwToken));
			break;
		default:
			break;

		}

		var response = context.getClient()
				.target(cfg.getTokenEndpoint())
				.request()
				.header(DocuWareService.X_REQUESTED_BY, DocuWareService.AXON_IVY_DOCUWARE_CONNECTOR)
				.accept(MediaType.APPLICATION_JSON)
				.property(SKIP_FILTER, Boolean.TRUE)
				.property(DocuWareService.CONFIG_KEY_PROPERTY, configKey)
				.post(Entity.form(payload));

		if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
			authError("fetchtoken").withMessage("Failed to fetch access token from '%s' for config '%s', response: %s.".formatted(cfg.getTokenEndpoint(), configKey, response))
			.withAttribute("status", response.getStatus())
			.withAttribute("payload", response.readEntity(String.class))
			.throwError();
		}

		var map = response.readEntity(new GenericType<Map<String, Object>>(Map.class));

		var token = new Token(map);
		token.setConfig(configKey);
		token.setConfigId(cfg.getConfigId());

		return token;
	}

	private String fetchEndpointUrl(ClientRequestContext context) {
		var configKey = getConfigKey(context);

		var cfg = Configuration.getKnownConfigurationOrDefault(configKey);

		var identityServiceInfoUrl = UriBuilder
				.fromPath(cfg.getUrl())
				.path(IDENTITY_SERVICE_INFO_URL)
				.build();

		Ivy.log().debug("Fetching identity service info from url ''{0}''", identityServiceInfoUrl);

		var response = context.getClient()
				.target(identityServiceInfoUrl)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.property(SKIP_FILTER, Boolean.TRUE)
				.property(DocuWareService.CONFIG_KEY_PROPERTY, configKey)
				.get();

		if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
			authError("fetchidentityserviceinfourl").withMessage("Failed to get Identity Service Info Url for config '%s', response: %s".formatted(configKey, response))
			.withAttribute("status", response.getStatus())
			.withAttribute("payload", response.readEntity(String.class))
			.throwError();
		}

		var map = response.readEntity(new GenericType<Map<String, Object>>(Map.class));

		var responseIdentityServiceUrl = (String)map.get(IDENTITY_SERVICE_URL);

		if(StringUtils.isBlank(responseIdentityServiceUrl)) {
			authError("noidentityserviceurl")
			.withMessage("Did not receive an identity service url for config '%s'".formatted(configKey))
			.throwError();
		}

		var identityServiceUrl = UriBuilder
				.fromPath(responseIdentityServiceUrl)
				.path(WELL_KNOWN_OPENID_CONFIGURATION)
				.build();

		Ivy.log().debug("Fetching identity service from url ''{0}''", identityServiceUrl);

		response = context.getClient()
				.target(identityServiceUrl)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.property(SKIP_FILTER, Boolean.TRUE)
				.property(DocuWareService.CONFIG_KEY_PROPERTY, configKey)
				.get();

		if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
			authError("fetchidentityserviceurl").withMessage("Failed to get Identity Service Url for config '%s', response: %s".formatted(configKey, response))
			.withAttribute("status", response.getStatus())
			.withAttribute("payload", response.readEntity(String.class))
			.throwError();
		}

		map = response.readEntity(new GenericType<Map<String, Object>>(Map.class));

		var responseTokenEndpointUrl = (String)map.get(TOKEN_ENDPOINT_URL);

		if(StringUtils.isBlank(responseTokenEndpointUrl)) {
			authError("notokenendpointurl")
			.withMessage("Did not receive a token endpoint URL for config '%s'".formatted(configKey))
			.throwError();
		}
		return responseTokenEndpointUrl;
	}

	/**
	 * Get the current config key used from the configuration.
	 * 
	 * @param context
	 * @return
	 */
	private String getConfigKey(ClientRequestContext context) {
		return Configuration.knownOrDefaultKey((String)context.getConfiguration().getProperty(DocuWareService.CONFIG_KEY_PROPERTY));
	}

	/**
	 * Get the current config key used from the request context.
	 * 
	 * @param context
	 * @return
	 */
	private String getContextKey(ClientRequestContext context) {
		return Configuration.knownOrDefaultKey((String)context.getProperty(DocuWareService.CONFIG_KEY_PROPERTY));
	}

	/**
	 * Prepare authentication error.
	 * 
	 * @return
	 */
	private BpmPublicErrorBuilder authError(String type) {
		return BpmError.create(DocuWareService.DOCUWARE_ERROR + "authentication:" + type);
	}
}
