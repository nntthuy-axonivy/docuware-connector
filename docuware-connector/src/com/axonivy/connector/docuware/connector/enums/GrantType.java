package com.axonivy.connector.docuware.connector.enums;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Functions;

public enum GrantType {
	PASSWORD("password"), DW_TOKEN("dwtoken"), TRUSTED("trusted");

	private String code;
	private static final Map<String, GrantType> CODE_MAP = Stream.of(values()).collect(Collectors.toMap(GrantType::code, Functions.identity())); 

	private GrantType(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static GrantType of(String code) {
		return CODE_MAP.get(code);
	}
}
