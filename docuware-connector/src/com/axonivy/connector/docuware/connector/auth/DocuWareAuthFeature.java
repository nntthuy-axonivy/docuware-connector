package com.axonivy.connector.docuware.connector.auth;

import java.io.IOException;
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
import com.axonivy.connector.docuware.connector.auth.oauth.Configuration;
import com.axonivy.connector.docuware.connector.auth.oauth.Token;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;

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
			var skip = context.getProperty(SKIP_FILTER);
			if(skip == Boolean.TRUE) {
				Ivy.log().debug("Request filter is skipped for ''{0}''.", context.getUri());
			}
			else {
				String accessToken = getAccessToken(context);
				context.getHeaders().add(AUTHORIZATION, BEARER + accessToken);
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
		var config = getConfig(context);

		var token = DocuWareService.get().getCachedToken(config);

		if (token == null || token.isExpired() || !DocuWareService.get().isValidConfigId(config, token.getConfigId())) {
			token = getNewAccessToken(context);
			DocuWareService.get().setCachedToken(config, token);
			Ivy.log().debug("Cached a new token: {0}", token);
		}

		if (!token.hasAccessToken()) {
			DocuWareService.get().setCachedToken(config, null);
			authError("accesstoken")
			.withMessage("Failed to get access token for config '%s' and token %s".formatted(config, token))
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
	private Token getNewAccessToken(ClientRequestContext context) {
		var config = getConfig(context);

		var configuration = DocuWareService.get().getCachedConfiguration(config);

		if(configuration == null || !DocuWareService.get().isValidConfigId(config, configuration.getConfigId())) {
			configuration = getNewConfiguration(context);
			DocuWareService.get().setCachedConfiguration(config, configuration);
			Ivy.log().debug("Cached a new configuration: {0}", configuration);
		}

		Ivy.log().debug("Fetching token from url ''{0}''", configuration.getTokenEndpoint());

		var grantType = DocuWareService.get().getConfigGrantType(config, null);

		if(grantType == null) {
			authError("missingGrantType")
			.withMessage("GrantType is missing for config '%s'".formatted(config))
			.throwError();
		}

		var payload = new MultivaluedHashMap<String, String>();

		payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_GRANT_TYPE, List.of(grantType.code()));
		payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_SCOPE, List.of(SCOPE));
		payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_CLIENT_ID, List.of(CLIENT_ID));

		switch(grantType) {
		case PASSWORD:
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_USERNAME, List.of(DocuWareService.get().getConfigVar(config, DocuWareVariable.USERNAME, null)));
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_PASSWORD, List.of(DocuWareService.get().getConfigVar(config, DocuWareVariable.PASSWORD, null)));
			break;
		case TRUSTED:
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_USERNAME, List.of(DocuWareService.get().getConfigVar(config, DocuWareVariable.USERNAME, null)));
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_PASSWORD, List.of(DocuWareService.get().getConfigVar(config, DocuWareVariable.PASSWORD, null)));
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_IMPERSONATE_NAME, List.of(DocuWareService.get().getImpersonateUserName(configuration.getImpersonateStrategy())));
			break;
		case DW_TOKEN:
			payload.put(DocuWareService.ACCESS_TOKEN_REQUEST_TOKEN, List.of(DocuWareService.get().getDwToken(configuration.getDwTokenStrategy())));
			break;
		default:
			break;

		}


		// TODO send correct payload
		// TODO handle connection parameters like timeout and logging
		var response = context.getClient()
				.target(configuration.getTokenEndpoint())
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.property(SKIP_FILTER, Boolean.TRUE)
				.post(Entity.form(payload));

		if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
			authError("fetchtoken").withMessage("Failed to get Token from '%s' for config '%s', response: %s".formatted(configuration.getTokenEndpoint(), config, response))
			.withAttribute("status", response.getStatus())
			.withAttribute("payload", response.readEntity(String.class))
			.throwError();
		}

		var map = response.readEntity(new GenericType<Map<String, Object>>(Map.class));

		var token = new Token(map);
		token.setConfig(config);
		token.setConfigId(configuration.getConfigId());

		// TODO test token 
		// return new Token(map);
		return token;
	}

	private Configuration getNewConfiguration(ClientRequestContext context) {
		var config = getConfig(context);
		var url = DocuWareService.get().getConfigVar(config, DocuWareVariable.URL, null);
		if(url == null) {
			authError("noidentityserviceinfourl")
			.withMessage("The url is not set for config '%s'".formatted(config))
			.throwError();
		}

		var identityServiceInfoUrl = UriBuilder
				.fromPath(url)
				.path(IDENTITY_SERVICE_INFO_URL)
				.build();

		Ivy.log().debug("Fetching identity service info from url ''{0}''", identityServiceInfoUrl);

		// TODO handle connection parameters like timeout and logging
		var response = context.getClient()
				.target(identityServiceInfoUrl)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.property(SKIP_FILTER, Boolean.TRUE)
				.get();

		if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
			authError("fetchidentityserviceinfourl").withMessage("Failed to get Identity Service Info Url for config '%s', response: %s".formatted(config, response))
			.withAttribute("status", response.getStatus())
			.withAttribute("payload", response.readEntity(String.class))
			.throwError();
		}

		var map = response.readEntity(new GenericType<Map<String, Object>>(Map.class));

		var responseIdentityServiceUrl = (String)map.get(IDENTITY_SERVICE_URL);

		if(StringUtils.isBlank(responseIdentityServiceUrl)) {
			authError("noidentityserviceurl")
			.withMessage("Did not receive an identity service url for config '%s'".formatted(config))
			.throwError();
		}

		var identityServiceUrl = UriBuilder
				.fromPath(responseIdentityServiceUrl)
				.path(WELL_KNOWN_OPENID_CONFIGURATION)
				.build();

		Ivy.log().debug("Fetching identity service from url ''{0}''", identityServiceUrl);

		// TODO handle connection parameters like timeout and logging
		response = context.getClient()
				.target(identityServiceUrl)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.property(SKIP_FILTER, Boolean.TRUE)
				.get();

		if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
			authError("fetchidentityserviceurl").withMessage("Failed to get Identity Service Url for config '%s', response: %s".formatted(config, response))
			.withAttribute("status", response.getStatus())
			.withAttribute("payload", response.readEntity(String.class))
			.throwError();
		}

		map = response.readEntity(new GenericType<Map<String, Object>>(Map.class));

		var responseTokenEndpointUrl = (String)map.get(TOKEN_ENDPOINT_URL);

		if(StringUtils.isBlank(responseTokenEndpointUrl)) {
			authError("notokenendpointurl")
			.withMessage("Did not receive a token endpoint URL for config '%s'".formatted(config))
			.throwError();
		}
		var configuration = DocuWareService.get().createConfiguration(config);
		configuration.setTokenEndpoint(responseTokenEndpointUrl);
		return configuration;
	}

	/**
	 * Get the current config used.
	 * 
	 * @param context
	 * @return
	 */
	private String getConfig(ClientRequestContext context) {
		return DocuWareService.get().safeConfig((String)context.getConfiguration().getProperty("config"));
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
