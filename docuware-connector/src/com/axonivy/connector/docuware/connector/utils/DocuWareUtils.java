package com.axonivy.connector.docuware.connector.utils;

import static com.axonivy.connector.docuware.connector.DocuWareConstants.INSTANCE_PROPERTY_PATTERN;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.ACCESS_TOKEN;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.CONNECT_TIMEOUT;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.DEFAULT_INSTANCE;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.FILE_CABINET_ID;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.GRANT_TYPE;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.HOST;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.LOGIN_TOKEN;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.PASSWORD;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.TRUSTED_USERNAME;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.TRUSTED_USER_PASSWORD;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.USERNAME;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.axonivy.connector.docuware.connector.enums.GrantType;
import com.fasterxml.jackson.databind.JsonNode;

import ch.ivyteam.ivy.environment.Ivy;

public class DocuWareUtils {

  private DocuWareUtils() { }

  public static String getIvyVar(DocuWareVariable variable) {
    return Ivy.var().get(variable.getVariableName());
  }

  public static String setIvyVar(DocuWareVariable variable, String value) {
    return Ivy.var().set(variable.getVariableName(), value);
  }

  public static String getVariableValueByInstance(String instanceName, DocuWareVariable variable) {
    return Ivy.var().get(buildVariableKeyForInstance(instanceName, variable));
  }
  
  public static String setVariableByInstance(String instanceName, DocuWareVariable variable, String value) {
    return Ivy.var().set(buildVariableKeyForInstance(instanceName, variable), value);
  }

  public static String buildVariableKeyForInstance(String instanceName, DocuWareVariable variable) {
    return String.format(INSTANCE_PROPERTY_PATTERN, instanceName, variable.variableKey);
  }
  
  public static String getActiveVariableValue(DocuWareVariable variable) {
    var defaultInstanceName = Ivy.var().get(DEFAULT_INSTANCE.getVariableName());
    return getVariableValueByInstance(defaultInstanceName, variable);
  }

  public static JsonNode getWebTargetResponseAsJsonNode(URI targetURI) {
    Client client = ClientBuilder.newClient();
    Response response = null;
    try {
      WebTarget target = client.target(targetURI);
      response = target.request(MediaType.APPLICATION_JSON).get();
      if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
        String jsonResponse = response.readEntity(String.class);
        return JsonUtils.parseToJsonNode(jsonResponse);
      }
    } catch (Exception e) {
      Ivy.log().error("Error: status is {0} - {1}", response.getStatus(), response);
    } finally {
      response.close();
      client.close();
    }
    return null;
  }

  public static String generateLoginTokenBody() {
    return """
        {
          "TargetProducts": [
              "PlatformService"
          ],
          "Usage": "Multi",
          "Lifetime": "1.00:00:00"
        }
        """;
  }

  public static DocuWareEndpointConfiguration getDefaultActiveInstance() {
    var defaultInstanceName = Ivy.var().get(DEFAULT_INSTANCE.getVariableName());
    return StringUtils.isNoneBlank(defaultInstanceName) ? extractVariableByInstanceName(defaultInstanceName) : null;
  }

  public static DocuWareEndpointConfiguration extractVariableByInstanceName(String instanceName) {
    var config = new DocuWareEndpointConfiguration(instanceName);
    for (var docuVar : DocuWareVariable.values()) {
      getInstanceConfiguration(config, instanceName, docuVar);
    }
    return config;
  }

  public static DocuWareEndpointConfiguration getInstanceConfiguration(DocuWareEndpointConfiguration config,
      String instanceName, DocuWareVariable variable) {
    switch (variable) {
    case HOST:
      config.setHost(getVariableValueByInstance(instanceName, HOST));
      break;
    case GRANT_TYPE:
      String grantType = getVariableValueByInstance(instanceName, GRANT_TYPE);
      config.setGrantType(GrantType.of(grantType));
      break;
    case USERNAME:
      config.setUsername(getVariableValueByInstance(instanceName, USERNAME));
      break;
    case PASSWORD:
      config.setPassword(getVariableValueByInstance(instanceName, PASSWORD));
      break;
    case CONNECT_TIMEOUT:
      String connectTimeout = getVariableValueByInstance(instanceName, CONNECT_TIMEOUT);
      config.setConnectTimeout(NumberUtils.isCreatable(connectTimeout) ? NumberUtils.toInt(connectTimeout) : 0);
      break;
    case TRUSTED_USERNAME:
      config.setTrustedUserName(getVariableValueByInstance(instanceName, TRUSTED_USERNAME));
      break;
    case TRUSTED_USER_PASSWORD:
      config.setTrustedUserPassword(getVariableValueByInstance(instanceName, TRUSTED_USER_PASSWORD));
      break;
    case ACCESS_TOKEN:
      config.setAccessToken(getVariableValueByInstance(instanceName, ACCESS_TOKEN));
      break;
    case FILE_CABINET_ID:
      config.setFileCabinetId(getVariableValueByInstance(instanceName, FILE_CABINET_ID));
      break;
    case LOGIN_TOKEN:
      config.setLoginToken(getVariableValueByInstance(instanceName, LOGIN_TOKEN));
      break;
    default:
      break;
    }
    return config;
  }
}
