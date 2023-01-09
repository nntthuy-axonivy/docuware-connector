package com.axonivy.connector.docuware.connector;

public class DocuWareException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -152627765623687902L;
		
	private String httpCode;
	private String errorMessage;
	
	public DocuWareException() {
	}
	
	public DocuWareException(String errorMessage, String statusCode) {
		this.httpCode = statusCode;
		this.errorMessage = errorMessage;
	}

	public String getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(String httpCode) {
		this.httpCode = httpCode;
	}
	
	
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return String.format("DocuWare return an error with code: %s", this.getHttpCode());
	}
}
