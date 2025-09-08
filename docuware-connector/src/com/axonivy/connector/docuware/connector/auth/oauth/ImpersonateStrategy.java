package com.axonivy.connector.docuware.connector.auth.oauth;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.docuware.connector.DocuWareService;

import ch.ivyteam.ivy.bpm.error.BpmError;

/**
 * Represent an Impersonation Strategy.
 * <p>
 * The following syntax is implemented to determine the user's DW name for automatic impersonation.
 * <dl>
 * <dt>DW name is constant</dt>
 * <dd><code>dwuser</code></dd>
 * <dt>DW names are fixed for system (developer), anonymous and user</dt>
 * <dd><code>^fixed:system=&lt;dwuser&gt;,anonymous=&lt;dwuser&gt;,user=&lt;dwuser&gt;</code></dt>
 * <dt>DW names are fixed for system (developer) and anonymous but current ivy user name for other users</dt>
 * <dd>^ivy:system=&lt;dwuser&gt;,anonymous=&gt;dwuser&gt;</dd>
 * <dt>DW name is taken from an attribute of the current session (needs to be set by a service function)</dt>
 * <dd><code>^session</code></dd>
 * </dl>
 * </p>
 */
public class ImpersonateStrategy {
	private static final String IVY_USER = "user";
	private static final String ANONYMOUS_USER = "anonymous";
	private static final String SYSTEM_USER = "system";
	/**
	 * Very forgiving regex for impersonation 
	 */
	private static final Pattern IMP_USER_PATTERN = Pattern.compile(
			"""
			# ignore whitespace at start
			\\s*
			# start with a caret
			\\^\\s*
			# any character but no colon 
			(.+?)
			# a colon and the "right side" which is parsed in a second step
			(\\s*:\\s*(.+)?)?
			""",
			Pattern.COMMENTS);
	private Strategy strategy;
	private String systemUser;
	private String anonymousUser;
	private String ivyUser;
	private String config;

	private ImpersonateStrategy() {};

	/**
	 * Create a new {@link ImpersonateStrategy}.
	 * 
	 * @param config
	 * @param impersonateUser
	 * @return new strategy or <code>null</code> if impersonateUser is blank
	 */
	public static ImpersonateStrategy create(String config, String impersonateUser) {
		var valid = false;
		ImpersonateStrategy is = null;
		if(StringUtils.isNotBlank(impersonateUser)) {
			Exception ex = null;
			try {
				is = new ImpersonateStrategy();
				is.config = config;

				var m = IMP_USER_PATTERN.matcher(impersonateUser);

				if(m.matches()) {
					is.strategy = Strategy.valueOf(m.group(1).toUpperCase());
					var rest = m.group(3);
					if(rest != null) {
						for (var userdef : Arrays.asList(rest.split(","))) {
							var pair = userdef.split("=");
							var key = pair[0].strip();
							var val = pair.length > 1 ? pair[1].strip() : null;

							switch(key) {
							case SYSTEM_USER:
								is.systemUser = val;
								break;
							case ANONYMOUS_USER:
								is.anonymousUser = val;
								break;
							case IVY_USER:
								is.ivyUser = val;
								break;
							default:
								throw new IllegalArgumentException("Not a valid userkey: '%s'".formatted(key));
							}
						}
					}
				}
				else {
					is.strategy = Strategy.CONSTANT;
					var user = impersonateUser.strip();
					is.systemUser = user;
					is.anonymousUser = user;
					is.ivyUser = user;
				}


				// Check plausibility of finally determined names for system, anonymous and ivy user.
				if(is.strategy != null) {
					valid = switch(is.strategy) {
					case CONSTANT -> !StringUtils.isAnyBlank(is.systemUser, is.anonymousUser, is.ivyUser);
					case FIXED -> !StringUtils.isAnyBlank(is.systemUser, is.anonymousUser, is.ivyUser); 
					case IVY -> !StringUtils.isAnyBlank(is.systemUser, is.anonymousUser) && StringUtils.isBlank(is.ivyUser);
					default -> StringUtils.isAllBlank(is.systemUser, is.anonymousUser, is.ivyUser);
					};
				}
			} catch (Exception e) {
				ex = e;
			}
			if(!valid) {
				var error = BpmError.create(DocuWareService.DOCUWARE_ERROR + "invalidimpersonateUser")
						.withMessage("Invalid impersonate pattern: '%s'".formatted(impersonateUser));
				if(ex != null) {
					error.withCause(ex);
				}
				error.throwError();
			}
		}

		return is;
	}

	public String getConfig() {
		return config;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public String getSystemUser() {
		return systemUser;
	}

	public String getAnonymousUser() {
		return anonymousUser;
	}

	public String getIvyUser() {
		return ivyUser;
	}

	/**
	 * Impersonation Strategies.
	 */
	public enum Strategy {
		/**
		 * A constant DW name for all situations.
		 */
		CONSTANT,
		/**
		 * Fixed DW names defined for users, for system and for anonymous. 
		 */
		FIXED,
		/**
		 * The DW user name is the Iyv user and special users defined for system and anonymous. 
		 */
		IVY,
		/**
		 * The DW user name is taken from the session.
		 */
		SESSION;
	}
}
