package com.axonivy.connector.docuware.connector.managedbean;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.auth.OAuth2Feature;
import com.axonivy.connector.docuware.connector.auth.oauth.IdentityServiceContext;
import com.axonivy.connector.docuware.connector.auth.oauth.Token;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class RequestLoginTokenBean {

	private String host;
	private String username;
	private String password;
	private String loginToken;

	@PostConstruct
	public void init() {
		host = DocuWareService.get().getIvyVar(DocuWareVariable.HOST);
	}

	public void requestNewLoginToken() {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);

		Token token = generateNewIdentityToken();

		try {
			loginToken = DocuWareService.get().getLoginTokenString(token);
			if(loginToken != null) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						Ivy.cms().co("/Dialogs/com/axonivy/market/docuware/connector/RequestLoginToken/GotLoginToken"),
						Ivy.cms().co(
								"/Dialogs/com/axonivy/market/docuware/connector/RequestLoginToken/GotLoginTokenMessage"));
				FacesContext.getCurrentInstance().addMessage(null, message);
				DocuWareService.get().setIvyVar(DocuWareVariable.LOGIN_TOKEN, loginToken);
			}
		} catch (Exception e) {
			Ivy.log().error(e);
		}
	}

	public String generateLoginTokenBody() {
		return """
				{
				  "TargetProducts": [
				      "PlatformService"
				  ],
				  "Usage": "Multi",
				  "Lifetime": "1.00:00:00"
				}
				""";
	}

	public Token generateNewIdentityToken() {
		var tokenEndpointUrl = identifyTokenEndpointUrl();

		Token token = null;
		var map = new GenericType<Map<String, Object>>(Map.class);
		var paramsMap = OAuth2Feature.passwordParamsMap(username, password);
		var client = ClientBuilder.newClient();
		Response postResponse = null;
		try {
			var target = client.target(tokenEndpointUrl);
			postResponse = target.request(MediaType.APPLICATION_JSON).post(Entity.form(paramsMap));
			if (Family.SUCCESSFUL == postResponse.getStatusInfo().getFamily()) {
				token = new Token(postResponse.readEntity(map));
			}
		} catch (Exception e) {
			Ivy.log().error(e);
		} finally {
			postResponse.close();
			client.close();
		}
		return token;
	}

	public String identifyTokenEndpointUrl() {
		var identityServiceInfoURI = IdentityServiceContext.buildIdentityServiceInfoURI(host);
		var jsonData = DocuWareService.get().getWebTargetResponseAsJsonNode(identityServiceInfoURI);
		var identityURL = Optional.ofNullable(jsonData).map(IdentityServiceContext.extractIdentityServiceProperty())
				.orElse(null);

		var openIdConfigURI = IdentityServiceContext.buildOpenIdConfigurationURI(identityURL);
		jsonData = DocuWareService.get().getWebTargetResponseAsJsonNode(openIdConfigURI);
		return Optional.ofNullable(jsonData).map(IdentityServiceContext.extractTokenEndpointProperty()).orElse(null);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

}
