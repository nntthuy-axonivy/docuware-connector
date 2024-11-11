package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.axonivy.market.docuware.connector.demo.Data;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.security.ISession;

@IvyProcessTest(enableWebServer = true)
public class TestDocuWareDemo extends TestDocuWareConnector {

  @Test
  public void testOrganizations(BpmClient bpmClient, ISession session, AppFixture fixture, IApplication app) {
    prepareRestClient(app, fixture);
    ExecutionResult result = bpmClient.start().process("DocuWareDemo/organizations.ivp").execute();
    Data data = result.data().last();
    assertThat(data.getOrganizations().getOrganization()).hasSize(1);
  }

}
