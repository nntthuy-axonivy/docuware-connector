package com.axonivy.connector.docuware.connector.utils;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
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
}
