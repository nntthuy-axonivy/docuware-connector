package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareAuthFeature;
import com.axonivy.market.docuware.connector.demo.Data;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClient.Builder;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.security.ISession;

@IvyProcessTest(enableWebServer = true)
public class TestDocuWareDemo {

  @Test
  public void testOrganizations(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app) {
    prepareRestClient(app, fixture);
    ExecutionResult result = bpmClient.start().process("DocuWareDemo/organizations.ivp").execute();
    Data data = result.data().last();
    assertThat(data.getOrganizations().getOrganization()).hasSize(1);
  }

  private void prepareRestClient(IApplication app, AppFixture fixture) {
    fixture.var("docuware-connector.host", "TESTHOST");
    fixture.var("docuware-connector.username", "TESTUSER");
    fixture.var("docuware-connector.password", "TESTPASS");
    fixture.var("docuware-connector.hostid", "TESTHOSTID");
    RestClient restClient = RestClients.of(app).find("DocuWare");
    // change created client: use test url and a slightly different version of
    // the
    // DocuWare Auth feature
    Builder builder = RestClient.create(restClient.name()).uuid(restClient.uniqueId())
            .uri("http://{ivy.engine.host}:{ivy.engine.http.port}/{ivy.request.application}/api/docuWareMock")
            .description(restClient.description()).properties(restClient.properties());
    // use test feature instead of real one
    for (String feature : restClient.features()) {
      if (feature.contains(DocuWareAuthFeature.class.getCanonicalName())) {
        feature = DocuWareAuthTestFeature.class.getCanonicalName();
      }
      builder.feature(feature);
    }
    restClient = builder.toRestClient();
    RestClients.of(app).set(restClient);
  }
}
