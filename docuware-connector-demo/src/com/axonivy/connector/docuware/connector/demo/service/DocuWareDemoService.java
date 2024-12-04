package com.axonivy.connector.docuware.connector.demo.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ivyteam.ivy.cm.ContentObjectValue;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.scripting.objects.File;


public class DocuWareDemoService {
	private static DocuWareDemoService INSTANCE = new DocuWareDemoService();

	public static DocuWareDemoService get() {
		return INSTANCE;
	}

	public String getLog(Response response) {
		return getGenericLog(response, pw -> {
			String body = response.readEntity(String.class);

			pw.println();
			pw.format("Body:%n");
			pw.format(body);
		});
	}

	public String getLog(Response response, Object object) {
		return getGenericLog(response, pw -> {
			pw.print(object.toString());
		});
	}

	protected String getGenericLog(Response response, Consumer<PrintWriter> printWriterConsumer) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		pw.println("Response");
		pw.println("========");
		pw.println("Status: " + response.getStatusInfo());
		pw.println();
		pw.println("Headers");
		pw.println("=======");

		for (Entry<String, List<Object>> entry : response.getHeaders().entrySet()) {
			for (Object header : entry.getValue()) {
				pw.format("%s: %s%n", entry.getKey(), header);
			}
		}
		pw.println();
		
		pw.println("Result");
		pw.println("======");

		printWriterConsumer.accept(pw);

		pw.println();

		String result = sw.toString();
		Ivy.log().info("{0}", result);
		return result;
	}
	
	
	 public static java.io.File exportFromCMS(String cmsUri, String ext) throws IOException {		   
		 String file = StringUtils.removeStart(cmsUri, "/") + "." + ext;
		 java.io.File tempFile = new File(file, true).getJavaFile();
		 tempFile.getParentFile().mkdirs();
		    ContentObjectValue cov = Ivy.cms().root().child().file(cmsUri, ext).value().get();
		    try (var in = cov.read().inputStream();
		            var fos = new FileOutputStream(tempFile)) {
		      IOUtils.copy(in, fos);
		    }
		    return tempFile;
	}
}
