package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.TestTemplate;
import com.axonivy.market.docuware.connector.demo.Data;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;

@IvyProcessTest(enableWebServer = true)
public class TestDocuWareDemo extends TestDocuWareConnector {

  @TestTemplate
  public void testOrganizations(BpmClient bpmClient) {
    ExecutionResult result = bpmClient.start().process("DocuWareDemo/organizations.ivp").execute();
    Data data = result.data().last();
    assertThat(data.getOrganizations().getOrganization()).hasSize(1);
  }

}
