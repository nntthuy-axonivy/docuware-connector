package com.axonivy.market.docuware.connector.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.docuware.connector.oauth.DocuWareAuthFeature;

import io.swagger.v3.oas.annotations.Hidden;

@Path("docuWareMock")
@PermitAll
@Hidden
public class DocuWareServiceMock {

	/**
	 * Logon Call.
	 *
	 * Note, that this call is currently not used for the tests because calling a
	 * REST service from a feature in the Ivy Test environment is currently not
	 * fully supported.
	 *
	 * @param userName
	 * @param password
	 * @param hostId
	 * @param redirectToMyselfInCaseOfError
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("Account/Logon")
	public Response accountLogon(@FormParam("UserName") String userName, @FormParam("Password") String password,
			@FormParam("HostID") String hostId,
			@FormParam("RedirectToMyselfInCaseOfError") String redirectToMyselfInCaseOfError) {
		return Response.ok(load("json/account/logon.json")).type(MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("Organizations")
	public Response organizations(@Context HttpServletRequest req) {
		if (!isAuthenticated(req)) {
			// note: the real service would send details
			return Response.status(401).build();
		} else {
			return Response.ok(load("xml/organizations.xml")).type(MediaType.APPLICATION_XML).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("FileCabinets/{FileCabinetId}/Documents/{DocumentId}")
	public Response getDocument(@Context HttpServletRequest req, @PathParam(value = "FileCabinetId") String fileCabinetId,
			@PathParam(value = "DocumentId") String documentId) {
		if (!isAuthenticated(req)) {
			// note: the real service would send details
			return Response.status(401).build();
		} else {
			return Response.ok(load("xml/document.xml")).type(MediaType.APPLICATION_XML).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("FileCabinets/{FileCabinetId}/Documents/{documentId}/FileDownload")
	public Response downloadFile(@Context HttpServletRequest req, @PathParam(value = "FileCabinetId") String fileCabinetId, @PathParam(value = "documentId") String documentId)	throws IOException {
		if (!isAuthenticated(req)) {
			// note: the real service would send details
			return Response.status(401).build();
		} else {
			// File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
			// FIXME should be fixed when we get to this place
			File pdf = null;
			Response response = Response.ok(pdf).build();
			response.getHeaders().add("Content-Disposition", "attachment; filename=\"EURO rates.pdf\"; filename*=utf-8''%e2%82%ac%20rates.pdf");
			return response;
		}
	}

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("FileCabinets/{FileCabinetId}/Documents/{documentId}/Fields")
	public Response updateDocument(@Context HttpServletRequest req,
			@PathParam(value = "FileCabinetId") String fileCabinetId, @PathParam(value = "documentId") String documentId) {
		if (!isAuthenticated(req)) {
			// note: the real service would send details
			return Response.status(401).build();
		} else {
			return Response.ok(load("xml/documentIndexFields.xml")).type(MediaType.APPLICATION_XML).build();
		}
	}

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("FileCabinets/{FileCabinetId}/Documents/{DocumentId}")
	public Response deleteDocument(@Context HttpServletRequest req,
			@PathParam(value = "FileCabinetId") String fileCabinetId, @PathParam(value = "DocumentId") String documentId) {
		if (!isAuthenticated(req)) {
			// note: the real service would send details
			return Response.status(401).build();
		} else {
			if (documentId.equals(Constants.DOCUMENT_ID_ERROR)) {
				return Response.status(403).entity("xml/error.xml").type(MediaType.APPLICATION_XML).build();
			} else {
				return Response.ok().build();
			}
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("FileCabinets/{FileCabinetId}/Documents")
	public Response upload(@Context HttpServletRequest req, @PathParam(value = "FileCabinetId") String fileCabinetId,
			@QueryParam(value = "StoreDialogId") String storeDialogId) {
		if (!isAuthenticated(req)) {
			// note: the real service would send details
			return Response.status(401).build();
		} else {
			String path = "xml/document.xml";
			if (StringUtils.equals(storeDialogId, "" + Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG)) {
				path = "xml/documentStoreDialogId.xml";
			} else if (StringUtils.equals(storeDialogId, "" + Constants.EXPECTED_DOCUMENT_ID_FOR_STORE_DIALOG_2)) {
				path = "xml/documentStoreDialogId2.xml";
			}
			return Response.ok(load(path)).type(MediaType.APPLICATION_XML).build();
		}
	}

	private boolean isAuthenticated(HttpServletRequest req) {
		return StringUtils.isNoneBlank(req.getHeader(DocuWareAuthFeature.AUTHORIZATION));
	}

	private String load(String path) {
		try (InputStream is = DocuWareServiceMock.class.getResourceAsStream(path)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read resource: " + path);
		}
	}
}
