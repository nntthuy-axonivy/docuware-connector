package com.axonivy.connector.docuware.connector.exception;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.rest.client.internal.oauth2.RedirectToIdentityProvider;

@SuppressWarnings("restriction")
public class DocuWareException extends RuntimeException {

  private static final long serialVersionUID = 1154880703799063664L;

  public DocuWareException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocuWareException(String message) {
    BpmError.create(RedirectToIdentityProvider.OAUTH2_ERROR_CODE).withMessage(message).throwError();
  }

}
