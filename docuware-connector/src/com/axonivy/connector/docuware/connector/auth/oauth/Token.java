package com.axonivy.connector.docuware.connector.auth.oauth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ivyteam.util.date.Now;

/**
 * Token properties to cache.
 */
public class Token {
	private Instant created;
	private String config;
	private String configId;
	private Map<String, Object> values;
	public static final String EXPIRES_IN = "expires_in";
	public static final String ACCESS_TOKEN = "access_token";

	public Token() { }

	public Token(Map<String, Object> values) {
		this.values = values;
		this.created = Now.asInstant();
	}

	public Object value(String name) {
		return values.get(name);
	}

	public boolean hasAccessToken() {
		return StringUtils.isNotBlank(accessToken());
	}

	public String accessToken() {
		return (String) values.get(ACCESS_TOKEN);
	}

	@JsonIgnore
	public boolean isExpired() {
		if (created == null) {
			return true;
		}
		var expiresAt = created.plus(expiresIn(), ChronoUnit.SECONDS);
		return Instant.now().isAfter(expiresAt);
	}

	private int expiresIn() {
		var expiresIn = (Integer) values.get(EXPIRES_IN);
		if (expiresIn == null) {
			return Integer.MAX_VALUE;
		}
		return expiresIn.intValue();
	}

	public Instant getCreated() {
		return created;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}

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

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	@Override
	public String toString() {
		return "Token [created=%s, config=%s, configId=%s, values=%s]".formatted(created, config, configId,	values);
	}

}