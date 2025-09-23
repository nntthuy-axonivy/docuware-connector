package com.axonivy.market.docuware.connector.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response.Status.Family;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.oauth.Configuration;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest(enableWebServer = true)
public class DocuWareAuthTest extends DocuWareConnectorTest {

	@Test
	public void testGrantTypePassword(AppFixture fix) {
		DocuWareService.get().clearCaches();

		var rand1 = assertTokenEchoCall("passwordtest", "password:testuser:::*");
		assertTokenEchoCall("passwordtest", "password:testuser:::%s".formatted(rand1));
	}

	@Test
	public void testGrantTypeTrustedConstant(AppFixture fix) {
		DocuWareService.get().clearCaches();

		fix.var("docuwareConnector.trustedtest1.impersonateUser", "constant");

		// Get token for ANONYMOUS.
		var rand1 = assertTokenEchoCall("trustedtest1", "trusted:testuser:constant::*");

		// Get token for an ivy user.
		fix.loginUser("testuser1");
		assertTokenEchoCall("trustedtest1", "trusted:testuser:constant::%s".formatted(rand1));

		// Get token for SYSTEM.
		fix.loginUser("SYSTEM");
		assertTokenEchoCall("trustedtest1", "trusted:testuser:constant::%s".formatted(rand1));
	}

	@Test
	public void testGrantTypeTrustedIvyFixed(AppFixture fix) {
		DocuWareService.get().clearCaches();

		fix.var("docuwareConnector.trustedtest1.impersonateUser", "^fixed:system=sysuser,anonymous=anonuser,user=ivyuser");

		// Get token for ANONYMOUS.
		var rand1 = assertTokenEchoCall("trustedtest1", "trusted:testuser:anonuser::*");
		assertTokenEchoCall("trustedtest1", "trusted:testuser:anonuser::%s".formatted(rand1));

		// Get token for an ivy user.
		fix.loginUser("testuser1");
		var rand2 = assertTokenEchoCall("trustedtest1", "trusted:testuser:ivyuser::*");

		assertThat(rand2).isNotEqualTo(rand1);

		// Get token for SYSTEM.
		fix.loginUser("SYSTEM");
		var rand3 = assertTokenEchoCall("trustedtest1", "trusted:testuser:sysuser::*");

		assertThat(rand3).isNotEqualTo(rand1);
	}

	@Test
	public void testGrantTypeTrustedIvy(AppFixture fix) {
		DocuWareService.get().clearCaches();

		fix.var("docuwareConnector.trustedtest1.impersonateUser", "^ivy:system=sysuser,anonymous=anonuser");

		// Get token for ANONYMOUS.
		var rand1 = assertTokenEchoCall("trustedtest1", "trusted:testuser:anonuser::*");
		assertTokenEchoCall("trustedtest1", "trusted:testuser:anonuser::%s".formatted(rand1));

		// Get token for an ivy user.
		fix.loginUser("testuser1");
		var rand2 = assertTokenEchoCall("trustedtest1", "trusted:testuser:testuser1::*");

		assertThat(rand2).isNotEqualTo(rand1);

		// Get token for SYSTEM.
		fix.loginUser("SYSTEM");
		var rand3 = assertTokenEchoCall("trustedtest1", "trusted:testuser:sysuser::*");

		assertThat(rand3).isNotEqualTo(rand1);
	}

	@Test
	public void testGrantTypeTrustedUserFromSession(AppFixture fix) {
		DocuWareService.get().clearCaches();

		// Check error for unset session user.
		assertThatExceptionOfType(ProcessingException.class)
		.isThrownBy(() -> assertTokenEchoCall("trustedtest2", "trusted:testuser:::*"))
		.havingCause()
		.isInstanceOf(BpmError.class)
		.extracting(e -> ((BpmError)e).getErrorCode())
		.asString()
		.endsWith("fetchtoken:missingimpersonateuser");

		// Work with two session users for different configurations.
		var trusted2Cfg = Configuration.getKnownConfiguration("trustedtest2");
		trusted2Cfg.setSessionDocuwareUser("trusted_1");
		var rand21 = assertTokenEchoCall("trustedtest2", "trusted:testuser:trusted_1::*");
		trusted2Cfg.setSessionDocuwareUser("trusted_2");
		var rand22 = assertTokenEchoCall("trustedtest2", "trusted:testuser:trusted_2::*");

		var trusted3Cfg = Configuration.getKnownConfiguration("trustedtest3");
		trusted3Cfg.setSessionDocuwareUser("trusted_1");
		var rand31 = assertTokenEchoCall("trustedtest3", "trusted:testuser:trusted_1::*");
		trusted3Cfg.setSessionDocuwareUser("trusted_2");
		var rand32 = assertTokenEchoCall("trustedtest3", "trusted:testuser:trusted_2::*");

		// All tokens should be unique.
		assertThat(new String[] {rand21, rand22, rand31, rand32}).doesNotHaveDuplicates();

		// All tokens should have been cached.
		trusted2Cfg.setSessionDocuwareUser("trusted_1");
		assertTokenEchoCall("trustedtest2", "trusted:testuser:trusted_1::%s".formatted(rand21));
		trusted2Cfg.setSessionDocuwareUser("trusted_2");
		assertTokenEchoCall("trustedtest2", "trusted:testuser:trusted_2::%s".formatted(rand22));

		trusted3Cfg.setSessionDocuwareUser("trusted_1");
		assertTokenEchoCall("trustedtest3", "trusted:testuser:trusted_1::%s".formatted(rand31));
		trusted3Cfg.setSessionDocuwareUser("trusted_2");
		assertTokenEchoCall("trustedtest3", "trusted:testuser:trusted_2::%s".formatted(rand32));
	}

	@Test
	public void testGrantTypeDwTokenFromSession(AppFixture fix) {
		DocuWareService.get().clearCaches();

		// Check error for unset session token.
		assertThatExceptionOfType(ProcessingException.class)
		.isThrownBy(() -> assertTokenEchoCall("dwtokentest1", "dwtoken::::*"))
		.havingCause()
		.isInstanceOf(BpmError.class)
		.extracting(e -> ((BpmError)e).getErrorCode())
		.asString()
		.endsWith("fetchtoken:missingdwtoken");

		// Work with two session tokens for different configurations.
		var dw1 = UUID.randomUUID().toString();
		var dw2 = UUID.randomUUID().toString();

		var dwToken1Cfg = Configuration.getKnownConfiguration("dwtokentest1");
		dwToken1Cfg.setSessionDocuwareToken(dw1);
		var rand21 = assertTokenEchoCall("dwtokentest1", "dwtoken:::%s:*".formatted(dw1));
		dwToken1Cfg.setSessionDocuwareToken(dw2);
		var rand22 = assertTokenEchoCall("dwtokentest1", "dwtoken:::%s:*".formatted(dw2));

		var dwToken2Cfg = Configuration.getKnownConfiguration("dwtokentest2");
		dwToken2Cfg.setSessionDocuwareToken(dw1);
		var rand31 = assertTokenEchoCall("dwtokentest2", "dwtoken:::%s:*".formatted(dw1));
		dwToken2Cfg.setSessionDocuwareToken(dw2);
		var rand32 = assertTokenEchoCall("dwtokentest2", "dwtoken:::%s:*".formatted(dw2));

		// All tokens should be unique.
		assertThat(new String[] {rand21, rand22, rand31, rand32}).doesNotHaveDuplicates();

		// All tokens should have been cached.
		dwToken1Cfg.setSessionDocuwareToken(dw1);
		assertTokenEchoCall("dwtokentest1", "dwtoken:::%s:%s".formatted(dw1, rand21));
		dwToken1Cfg.setSessionDocuwareToken(dw2);
		assertTokenEchoCall("dwtokentest1", "dwtoken:::%s:%s".formatted(dw2, rand22));

		dwToken2Cfg.setSessionDocuwareToken(dw1);
		assertTokenEchoCall("dwtokentest2", "dwtoken:::%s:%s".formatted(dw1, rand31));
		dwToken2Cfg.setSessionDocuwareToken(dw2);
		assertTokenEchoCall("dwtokentest2", "dwtoken:::%s:%s".formatted(dw2, rand32));
	}

	/**
	 * Assert, that the expected token is returned by the echo call.
	 * 
	 * @param configKey
	 * @param expToken
	 * @return the last part of the received token (the random part)
	 */
	protected String assertTokenEchoCall(String configKey, String expToken) {
		var tokenParts = 5;
		assertThat(expToken).isNotBlank();
		var expParts = expToken.split(":");
		assertThat(expParts).hasSize(tokenParts);

		var rsp = getClient(configKey).path("TokenEcho").request().get();
		assertThat(rsp.getStatusInfo().getFamily()).isEqualTo(Family.SUCCESSFUL);
		var token = rsp.readEntity(String.class);

		assertThat(token).isNotBlank();

		// Strip the Bearer keyword
		var content = token.split(" ");
		assertThat(content).hasSize(2);

		// Split the special token.
		var parts = content[1].split(":");
		assertThat(parts).hasSize(tokenParts);

		for(int i=0; i<tokenParts; i++) {
			var exp = expParts[i];
			var got = parts[i];

			var name = switch(i) {
			case 0 -> "grantType";
			case 1 -> "userName";
			case 2 -> "impersonateName";
			case 3 -> "dwToken";
			case 4 -> "token";
			default -> "unknown";
			};

			if(exp.equals("*")) {
				assertThat(got).as(name).isNotEmpty();
			}
			else {
				assertThat(got).as(name).isEqualTo(exp);
			}
		}
		return parts[4];
	}
}
