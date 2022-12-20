package com.axonivy.market.docuware.connector;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareAuthFeature;
import com.axonivy.connector.docuware.connector.DocuWareAuthFeature.Property;

import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class TestDocuWareAuthFeature {

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

}
