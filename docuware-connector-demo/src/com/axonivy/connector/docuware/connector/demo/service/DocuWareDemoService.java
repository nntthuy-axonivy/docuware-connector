package com.axonivy.connector.docuware.connector.demo.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

import javax.ws.rs.core.Response;

import ch.ivyteam.ivy.environment.Ivy;


public class DocuWareDemoService {
	private static DocuWareDemoService INSTANCE = new DocuWareDemoService();
	private static final String CSP_HEADER = "Content-Security-Policy";

	public static DocuWareDemoService get() {
		return INSTANCE;
	}

	public String getJsonLog(Response response) {
		return getGenericLog(response, pw -> {
			var body = response.readEntity(String.class);

			pw.println();
			pw.format("Body:%n");
			pw.format(body);
		});
	}

	public String getLog(Response response) {
		return getGenericLog(response, null);
	}

	public String getLog(Response response, Object object) {
		return getGenericLog(response, pw -> {
			pw.println(object);
		});
	}

	protected String getGenericLog(Response response, Consumer<PrintWriter> printWriterConsumer) {
		var sw = new StringWriter();
		var pw = new PrintWriter(sw);

		pw.println("Response: Status: %s".formatted(response.getStatusInfo()));
		pw.println();
		pw.println("Headers");
		pw.println("=======");

		for (var entry : response.getHeaders().entrySet()) {
			for (var header : entry.getValue()) {
				pw.format("%s: %s%n", entry.getKey(), header);
			}
		}

		var csp = response.getHeaders().get(CSP_HEADER);
		if(csp != null) {
			pw.println();
			pw.println("Note: There is a '%s' header containing the value '%s'. This header can be configured in DocuWare and allows or disallows embedding of DocuWare pages into frames coming from other web-servers.".formatted(CSP_HEADER, csp));
		}

		if(printWriterConsumer != null) {
			pw.println();

			pw.println("Result");
			pw.println("======");

			printWriterConsumer.accept(pw);

			pw.println();
		}

		var result = sw.toString();
		Ivy.log().info("{0}", result);
		return result;
	}
}
