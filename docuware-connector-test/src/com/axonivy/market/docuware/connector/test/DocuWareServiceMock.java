package com.axonivy.market.docuware.connector.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.PermitAll;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;

import com.axonivy.connector.docuware.connector.DocuWareAuthFeature;
import com.axonivy.connector.docuware.connector.demo.DocuWareDemoService;

import ch.ivyteam.ivy.environment.Ivy;
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
  public Response getDocument(@Context HttpServletRequest req,
          @PathParam(value = "FileCabinetId") String fileCabinetId,
          @PathParam(value = "DocumentId") String documentId) {
    Ivy.log().fatal("22222!!!!!!!!!in mock" + documentId);
    if (!isAuthenticated(req)) {
      // note: the real service would send details
      return Response.status(401).build();
    } else {
      return Response.ok(load("xml/document.xml")).type(MediaType.APPLICATION_XML).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_XML)
  @Path("FileCabinets/{fileCabinetId}/Documents/{documentId}/FileDownload")
  public Response downloadFile(@Context HttpServletRequest req,
          @PathParam(value = "fileCabinetId") String fileCabinetId,
          @PathParam(value = "documentId") String documentId) throws IOException {
    if (!isAuthenticated(req)) {
      // note: the real service would send details
      return Response.status(401).build();
    } else {
      File pdf = DocuWareDemoService.exportFromCMS("/Files/uploadSample", "pdf");
      Response response = Response.ok(pdf).build();
      response.getHeaders().add("Content-Disposition", "ABC filename=DownloadedFile.pdf");
      return response;
    }
  }

  @PUT
  @Produces(MediaType.APPLICATION_XML)
  @Path("FileCabinets/{fileCabinetId}/Documents/{documentId}/Fields")
  public Response updateDocument(@Context HttpServletRequest req,
          @PathParam(value = "fileCabinetId") String fileCabinetId,
          @PathParam(value = "documentId") String documentId) {
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
          @PathParam(value = "FileCabinetId") String fileCabinetId,
          @PathParam(value = "DocumentId") String documentId) {
    if (!isAuthenticated(req)) {
      // note: the real service would send details
      return Response.status(401).build();
    } else {
      if (documentId.equals(Constants.DOCUMENT_ID_ERRROR_CASE)) {
        return Response.status(403).entity("xml/error.xml").type(MediaType.APPLICATION_XML).build();
      } else {
        return Response.ok().build();
      }
    }
  }

  @POST
  @Produces(MediaType.APPLICATION_XML)
  @Path("FileCabinets/{fileCabinetId}/Documents")
  public Response upload(@Context HttpServletRequest req,
          @PathParam(value = "fileCabinetId") String fileCabinetId) {
    if (!isAuthenticated(req)) {
      // note: the real service would send details
      return Response.status(401).build();
    } else {
      return Response.ok(load("xml/document.xml")).type(MediaType.APPLICATION_XML).build();
    }
  }

  private boolean isAuthenticated(HttpServletRequest req) {
    Map<String, Cookie> cookies = Stream.of(req.getCookies())
            .collect(Collectors.toMap(c -> c.getName(), c -> c));
    return cookies.containsKey(DocuWareAuthFeature.DocuWareCookies.DW_PLATFORM_AUTH_COOKIE)
            && cookies.containsKey(DocuWareAuthFeature.DocuWareCookies.DW_PLATFORM_BROWSER_ID);
  }

  private String load(String path) {
    try (InputStream is = DocuWareServiceMock.class.getResourceAsStream(path)) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to read resource: " + path);
    }
  }
}
