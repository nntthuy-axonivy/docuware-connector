package com.axonivy.connector.docuware.connector.oauth;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.docuware.connector.DocuWareService;

import ch.ivyteam.ivy.bpm.error.BpmError;

/**
 * Represent a Strategy.
 * <p>
 * The following syntax is implemented to determine the DW token for automatic impersonation.
 * <dl>
 * <dt>DW token is taken from an attribute of the current session (needs to be set by a service function)</dt>
 * <dd><code>^session</code></dd>
 * </dl>
 * </p>
 */
public class DwTokenStrategy {
	/**
	 * Very forgiving regex for DW Token 
	 */
	private static final Pattern DW_TOKEN_PATTERN = Pattern.compile(
			"""
			# ignore whitespace at start
			\\s*
			# start with a caret
			\\^\\s*
			# any character but no colon 
			(.+?)
			""",
			Pattern.COMMENTS);
	private Strategy strategy;

	private DwTokenStrategy() {};

	/**
	 * Create a new {@link DwTokenStrategy}.
	 * 
	 * @param dwToken 
	 * @return new strategy or <code>null</code> if doToken is blank.
	 */
	public static DwTokenStrategy create(String dwToken) {
		var valid = false;
		DwTokenStrategy ts = null;
		if(StringUtils.isNotBlank(dwToken)) {
			Exception ex = null;
			try {
				ts = new DwTokenStrategy();

				var m = DW_TOKEN_PATTERN.matcher(dwToken);

				if(m.matches()) {
					ts.strategy = Strategy.valueOf(m.group(1).toUpperCase());

				}

				// Check plausibility.
				if(ts.strategy != null) {
					valid = true;
				}
			} catch (Exception e) {
				ex = e;
			}

			if(!valid) {
				var error = BpmError.create(DocuWareService.DOCUWARE_ERROR + "invaliddwToken")
						.withMessage("Invalid dw token pattern: '%s'".formatted(dwToken));
				if(ex != null) {
					error.withCause(ex);
				}
				error.throwError();
			}

		}

		return ts;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * Impersonation Strategies.
	 */
	public enum Strategy {
		/**
		 * The DW token is taken from the session.
		 */
		SESSION;
	}
}
