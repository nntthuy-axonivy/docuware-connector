package com.axonivy.connector.docuware.connector.auth.oauth;

import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.ACCESS_TOKEN;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.DEFAULT_INSTANCE;

import org.apache.commons.lang3.ObjectUtils;

import com.axonivy.connector.docuware.connector.utils.DocuWareUtils;
import com.axonivy.connector.docuware.connector.utils.JsonUtils;

import ch.ivyteam.ivy.environment.Ivy;

public class VarTokenStore {
  private final String varName;

  public static VarTokenStore get() {
    var defaultInstanceName = Ivy.var().get(DEFAULT_INSTANCE.getVariableName());
    ObjectUtils.requireNonEmpty(defaultInstanceName, "No configured defaultInstance!");
    String accessTokenByInstanceKey = DocuWareUtils.buildVariableKeyForInstance(defaultInstanceName, ACCESS_TOKEN);
    ObjectUtils.requireNonEmpty(accessTokenByInstanceKey, "Cannot get accesstoken var by requested instance!");
    return new VarTokenStore(accessTokenByInstanceKey);
  }

  VarTokenStore(String varName) {
    this.varName = varName;
  }

  /**
   * Loads Variable and tries to create the token object {@link Token}
   * 
   * @return
   */
  public Token getToken() {
    String tokenVar = Ivy.var().get(varName);
    return JsonUtils.convertJsonToObject(tokenVar, Token.class);
  }

  /**
   * Stores token object {@link Token} as a json to Variable
   * 
   * @param token
   */
  public void setToken(Token token) {
    String tokenString = JsonUtils.writeObjectAsJson(token);
    Ivy.var().set(varName, tokenString);
  }
}
