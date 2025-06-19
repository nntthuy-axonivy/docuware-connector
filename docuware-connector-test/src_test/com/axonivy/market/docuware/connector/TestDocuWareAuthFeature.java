package com.axonivy.market.docuware.connector;

import javax.ws.rs.client.ClientBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.auth.OAuth2Feature;
import com.axonivy.connector.docuware.connector.auth.OAuth2Feature.Property;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class TestDocuWareAuthFeature {

  @Test
  void noHost() {
    Assertions.assertThatThrownBy(() ->
              ClientBuilder.newClient().register(new OAuth2Feature())
                .property(Property.USERNAME, "Reguel")
                .property(Property.PASSWORD, "notMySecret")
                .target("someResource").request().get())
        .isInstanceOf(BpmError.class).hasMessageContaining("Missing property 'PATH.host'");
  }

}
