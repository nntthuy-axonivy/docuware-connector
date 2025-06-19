package com.axonivy.connector.docuware.connector.auth.oauth;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.core.UriBuilder;

import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.auth.OAuth2Feature.Property;
import com.axonivy.connector.docuware.connector.exception.DocuWareException;
import com.fasterxml.jackson.databind.JsonNode;

import ch.ivyteam.ivy.rest.client.FeatureConfig;

public class IdentityServiceContext {

	private static final String HTTPS_PROTOCOL = "https://";
	private static final String DEFAULT_PLATFORM = "docuware/platform";
	private static final String IDENTITY_SERVICE_INFO_URL = "Home/IdentityServiceInfo";
	private static final String OPEN_ID_CONFIGURATION_URL = ".well-known/openid-configuration";

	public final FeatureConfig config;

	public IdentityServiceContext(FeatureConfig featureConfig) {
		this.config = featureConfig;
	}

	private String fetchIdentityServiceUrl() {
		var docuWareHost = config.readMandatory(Property.HOST);
		var identityServiceInfoURL = buildIdentityServiceInfoURI(docuWareHost);
		var jsonResponse = DocuWareService.get().getWebTargetResponseAsJsonNode(identityServiceInfoURL);
		var identityServiceUrl = Optional.ofNullable(jsonResponse).map(extractIdentityServiceProperty());
		if (identityServiceUrl.isEmpty()) {
			throw new DocuWareException("Cannot get IdentityServiceUrl property from DocuWare");
		}
		return identityServiceUrl.get();
	}

	public String identifyTokenEndpointUrl() {
		var identityServiceUrl = fetchIdentityServiceUrl();
		var openidConfiguration = buildOpenIdConfigurationURI(identityServiceUrl);
		var jsonResponse = DocuWareService.get().getWebTargetResponseAsJsonNode(openidConfiguration);
		var tokenEndpoint = Optional.ofNullable(jsonResponse).map(extractTokenEndpointProperty());
		if (tokenEndpoint.isEmpty()) {
			throw new DocuWareException("Cannot get token_endpoint property from DocuWare");
		}
		return tokenEndpoint.get();
	}

	public static Function<? super JsonNode, ? extends String> extractIdentityServiceProperty() {
		return json -> json.get(IdentityServiceProperty.IDENTITY_SERVICE_PROPERTY.field).asText();
	}

	public static Function<? super JsonNode, ? extends String> extractTokenEndpointProperty() {
		return json -> json.get(IdentityServiceProperty.TOKEN_ENDPOINT.field).asText();
	}

	public static URI buildIdentityServiceInfoURI(String host) {
		Objects.requireNonNull(host, "Host must not be empty");
		return UriBuilder.fromPath(HTTPS_PROTOCOL).path(host).path(DEFAULT_PLATFORM).path(IDENTITY_SERVICE_INFO_URL)
				.build();
	}

	public static URI buildOpenIdConfigurationURI(String identityServiceUrl) {
		Objects.requireNonNull(identityServiceUrl, "IdentityServiceUrl must not be empty");
		return UriBuilder.fromPath(identityServiceUrl).path(OPEN_ID_CONFIGURATION_URL).build();
	}

	public enum IdentityServiceProperty {
		IDENTITY_SERVICE_PROPERTY("IdentityServiceUrl"), TOKEN_ENDPOINT("token_endpoint");

		public String field;

		private IdentityServiceProperty(String field) {
			this.field = field;
		}
	}
}
