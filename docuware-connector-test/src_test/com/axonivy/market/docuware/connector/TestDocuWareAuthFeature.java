package com.axonivy.market.docuware.connector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareAuthFeature;
import com.axonivy.connector.docuware.connector.DocuWareAuthFeature.Property;

import ch.ivyteam.ivy.environment.IvyTest;
import ch.ivyteam.ivy.rest.client.RestClientFactoryConstants;

@IvyTest
public class TestDocuWareAuthFeature {

  private static final String DW_REST_CLIENT_ID = "02d1eec1-32e9-4316-afc3-793448486203";

  @Test
  void noHost() {
    var ft = new DocuWareAuthFeature();
    Assertions.assertThatThrownBy(()->ClientBuilder.newClient().register(ft)
      .property(Property.USERNAME, "Reguel")
      .property(Property.PASSWORD, "notMySecret")
      .property(Property.HOSTID, "axoooooniiiivy")
      .target("someResource")
      .request().get())
    .isInstanceOf(ProcessingException.class)
    .hasMessageContaining("variable 'docuware-connector.host'");
  }

  @Test
  void noLogonUrl_resolveAuto() {
    var logon = new AtomicReference<String>();
    var ft = new DocuWareAuthFeature() {
      @Override
      protected Response logon(ClientRequestContext reqContext, Form form, String target) {
        logon.set(target);
        return Response.accepted().build();
      }
    };
    try {
      ClientBuilder.newClient().register(ft)
      .property(Property.USERNAME, "Reguel")
      .property(Property.PASSWORD, "notMySecret")
      .property(Property.HOSTID, "axoooooniiiivy")
      .property(RestClientFactoryConstants.PROPERTY_CLIENT_ID, DW_REST_CLIENT_ID)
      .property("jersey.config.client.connectTimeout", 1)
      .target("http://0.0.0.0/someResource")
      .request().get();
    } catch (Exception ex) {
      // expected
    } finally {
      assertThat(logon.get())
        .isEqualTo("https://0.0.0.0/docuware/platform/Account/Logon");
    }
  }

}
