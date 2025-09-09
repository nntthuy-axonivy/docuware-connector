package com.axonivy.market.docuware.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.docuware.connector.auth.oauth.ImpersonateStrategy;
import com.axonivy.connector.docuware.connector.auth.oauth.ImpersonateStrategy.Strategy;

import ch.ivyteam.ivy.bpm.error.BpmError;

public class TestImpersonateStrategy {
	@Test
	public void testDirect() {
		var s = ImpersonateStrategy.create("pst");
		assertThat(s.getStrategy()).isEqualTo(Strategy.CONSTANT);
		assertThat(s.getSystemUser()).isEqualTo("pst");
		assertThat(s.getAnonymousUser()).isEqualTo("pst");
		assertThat(s.getIvyUser()).isEqualTo("pst");

		s = ImpersonateStrategy.create(" pst ");
		assertThat(s.getStrategy()).isEqualTo(Strategy.CONSTANT);
		assertThat(s.getSystemUser()).isEqualTo("pst");
		assertThat(s.getAnonymousUser()).isEqualTo("pst");
		assertThat(s.getIvyUser()).isEqualTo("pst");
	}

	@Test
	public void testFixed() {
		var s = ImpersonateStrategy.create("^fixed:system=sys,anonymous=an,user=us");
		assertThat(s.getStrategy()).isEqualTo(Strategy.FIXED);
		assertThat(s.getSystemUser()).isEqualTo("sys");
		assertThat(s.getAnonymousUser()).isEqualTo("an");
		assertThat(s.getIvyUser()).isEqualTo("us");
	}

	@Test
	public void testIvy() {
		var s = ImpersonateStrategy.create("^ivy:system=sys,anonymous=an");
		assertThat(s.getStrategy()).isEqualTo(Strategy.IVY);
		assertThat(s.getSystemUser()).isEqualTo("sys");
		assertThat(s.getAnonymousUser()).isEqualTo("an");
		assertThat(s.getIvyUser()).isNull();
	}

	@Test
	public void testSession() {
		var s = ImpersonateStrategy.create("^session");
		assertThat(s.getStrategy()).isEqualTo(Strategy.SESSION);
		assertThat(s.getSystemUser()).isNull();
		assertThat(s.getAnonymousUser()).isNull();
		assertThat(s.getIvyUser()).isNull();

		s = ImpersonateStrategy.create(" ^ session : ");
		assertThat(s.getStrategy()).isEqualTo(Strategy.SESSION);
		assertThat(s.getSystemUser()).isNull();
		assertThat(s.getAnonymousUser()).isNull();
		assertThat(s.getIvyUser()).isNull();
	}

	@Test
	public void testSpaces() {
		var s = ImpersonateStrategy.create("  ^  fixed  :  anonymous  =  an  ,  user  =  us  ,  system  =  sys  ");
		assertThat(s.getStrategy()).isEqualTo(Strategy.FIXED);
		assertThat(s.getSystemUser()).isEqualTo("sys");
		assertThat(s.getAnonymousUser()).isEqualTo("an");
		assertThat(s.getIvyUser()).isEqualTo("us");
	}

	@Test
	public void testInvalid() {
		assertThatExceptionOfType(BpmError.class).isThrownBy(() -> ImpersonateStrategy.create("^dummy"));
		assertThatExceptionOfType(BpmError.class).isThrownBy(() -> ImpersonateStrategy.create("^fixed:system=sys,anonymous=an"));
		assertThatExceptionOfType(BpmError.class).isThrownBy(() -> ImpersonateStrategy.create("^ivy:system=sys,anonymous=an,user=bla"));
		assertThatExceptionOfType(BpmError.class).isThrownBy(() -> ImpersonateStrategy.create("^session:system=nothere"));
	}
}
