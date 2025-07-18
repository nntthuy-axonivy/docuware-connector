package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.axonivy.market.docuware.connector.context.MultiEnvironmentContextProvider;
import com.axonivy.market.docuware.connector.utils.DocuwareTestUtils;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClients;

@IvyProcessTest(enableWebServer = true)
@ExtendWith(MultiEnvironmentContextProvider.class)
public class TestDocuWareConnector {

  @BeforeEach
  void beforeEach(ExtensionContext context, AppFixture fixture, IApplication app) {
    DocuwareTestUtils.setUpConfigForContext(context.getDisplayName(), fixture, app);
  }

  @AfterEach
  void afterEach(ExtensionContext context, AppFixture fixture, IApplication app) {
    RestClients clients = RestClients.of(app);
    clients.remove("DocuWare");
  }
  
  public <T> void assertServiceErrorCodeIs404(T data, Function<T, Integer> errorCodeExtractor) {
    int errorCode = errorCodeExtractor.apply(data);
    assertThat(errorCode).isEqualTo(404);
  }
}
