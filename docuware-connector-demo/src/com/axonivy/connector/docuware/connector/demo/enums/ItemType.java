package com.axonivy.connector.docuware.connector.demo.enums;

public enum ItemType {
	DECIMAL("Decimal"), STRING("String"), INT("Int");

	private final String value;

	private ItemType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
