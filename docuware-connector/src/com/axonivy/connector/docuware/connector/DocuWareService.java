package com.axonivy.connector.docuware.connector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.axonivy.connector.docuware.connector.auth.oauth.Configuration;
import com.axonivy.connector.docuware.connector.auth.oauth.GlobalVarConfiguration;
import com.axonivy.connector.docuware.connector.auth.oauth.Token;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.axonivy.connector.docuware.connector.enums.GrantType;
import com.docuware.dev.schema._public.services.platform.CheckInReturnDocument;
import com.docuware.dev.schema._public.services.platform.Document;
import com.docuware.dev.schema._public.services.platform.DocumentIndexField;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.security.ISecurityConstants;
import ch.ivyteam.util.StringUtil;

public class DocuWareService {
	/*
	 * This is the format: /Date(1652285631000)/
	 */
	protected static UUID CLIENT_ID = UUID.fromString("02d1eec1-32e9-4316-afc3-793448486203");
	public static final String CONFIG_KEY_PROPERTY = "configKey";
	public static final String APP_ATT_TOKEN_PREFIX = Token.class.getCanonicalName();

	protected static final Pattern DATE_PATTERN = Pattern.compile("/Date\\(([0-9]+)\\)/");
	protected static final String PROPERTIES_FILE_NAME = "document";
	protected static final String PROPERTIES_FILE_EXTENSION = ".json";
	protected static final String PROPERTIES_FILE_CHARSET = "UTF-8";
	protected static final String CONTENT_DISPOSITION = "Content-Disposition";
	protected static final String RESPONSE_XML_ERROR_NODE = "Error";
	protected static final String RESPONSE_XML_MESSAGE_NODE = "Message";
	protected static final String STORE_DIALOG_ID = "storeDialogId";
	protected static final DocuWareService INSTANCE = new DocuWareService();
	protected static ObjectMapper objectMapper;
	public static final String DOCUWARE_ERROR = "docuware:connector:";

	public static DocuWareService get() {
		return INSTANCE;
	}

	public static final String ACCESS_TOKEN_REQUEST_GRANT_TYPE = "grant_type";
	public static final String ACCESS_TOKEN_REQUEST_CLIENT_ID = "client_id";
	public static final String ACCESS_TOKEN_REQUEST_SCOPE = "scope";
	public static final String ACCESS_TOKEN_REQUEST_TOKEN = "token";
	public static final String ACCESS_TOKEN_REQUEST_IMPERSONATE_NAME = "impersonateName";
	public static final String AUTHORIZATION_CODE = "authorization_code";
	public static final String ACCESS_TOKEN_REQUEST_USERNAME = "username";
	public static final String ACCESS_TOKEN_REQUEST_PASSWORD = "password";
	public static final String RESPONSE_STATUS_CODE_ATTRIBUTE = "RestClientResponseStatusCode";


	/**
	 * Get the cached token from the grant-type specific store.
	 * 
	 * @param configKey
	 * @param extra
	 * @return
	 */
	public Token getCachedToken(String configKey, String extra) {
		var key = createTokenCacheKey(configKey, extra);
		Token token = null;
		try {
			token = (Token)IApplication.current().getAttribute(key);
		} catch (ClassCastException e) {
			Ivy.log().error("Cache contained an old version of the token class, ignoring it.");
		}
		return token;
	}

	/**
	 * Set the cached token to the grant-type specific store.
	 * 
	 * @param config
	 * @param extra 
	 * @param token
	 */
	public void setCachedToken(String config, String extra, Token token) {
		var key = createTokenCacheKey(config, extra);
		IApplication.current().setAttribute(key, token);
	}

	/**
	 * Get all available Docuware configurations.
	 * 
	 * @return
	 */
	public Collection<String> getConfigs() {
		// Search for all variables in our namespace containing at least one child variable.
		var configPattern = Pattern.compile("%s[.]([^.]+)[.].+".formatted(GlobalVarConfiguration.DOCUWARE_CONNECTOR_VAR));

		return Ivy.var().names().stream()
				.map(n -> configPattern.matcher(n))
				.filter(m -> m.matches())
				.map(m -> m.group(1))
				.collect(Collectors.toSet());
	}

	/**
	 * Clear all cached configurations and tokens.
	 */
	public String clearCaches() {
		try (var sw = new StringWriter();
				var pw = new PrintWriter(sw)) {
			var cachedNames = IApplication.current().getAttributeNames().stream()
					.filter(n -> n.startsWith(Configuration.APP_ATT_CONFIG_PREFIX) || n.startsWith(APP_ATT_TOKEN_PREFIX))
					.sorted()
					.toList();

			pw.println("Removing caches:");
			for (var name : cachedNames) {
				IApplication.current().removeAttribute(name);
				pw.println(name);
			}

			return sw.toString();
		} catch (IOException e) {
			throw new RuntimeException("Error while removing caches.", e);
		}
	}

	/**
	 * @deprecated use String version with inheritance instead
	 * @param variable
	 * @return
	 */
	@Deprecated
	public String getIvyVar(DocuWareVariable variable) {
		return Ivy.var().get(variable.varName());
	}

	/**
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public GrantType getIvyVarGrantType() {
		var type = getIvyVar(DocuWareVariable.GRANT_TYPE);
		return Optional.ofNullable(GrantType.of(type)).orElse(GrantType.PASSWORD);
	}


	/**
	 * Get the Integration URL for embedding DocuWare into an IFrame including an optional organizationGuid (if more than one org is available).
	 * 
	 * @param configKey
	 * @param organizationGuid 
	 * @return
	 */
	public URIBuilder getIntegrationUrl(String configKey, String organizationGuid) {
		return createUriBuilder(configKey, "WebClient", organizationGuid, "Integration");
	}


	/**
	 * Get the client for a configuration.
	 * 
	 * @param configKey
	 * @return
	 */
	public WebTarget getClient(String configKey) {
		return Ivy.rest().client(CLIENT_ID).property(CONFIG_KEY_PROPERTY, Configuration.knownOrDefaultKey(configKey));

	}

	/**
	 * Create a URIBuilder into DowuWare with pathSegments.
	 * 
	 * @param configKey
	 * @param pathSegments
	 * @return
	 */
	public URIBuilder createUriBuilder(String configKey, String...pathSegments) {
		var cfg = Configuration.getKnownConfigurationOrDefault(configKey);
		try {
			var builder = new URIBuilder(cfg.getUrl());
			addPathSegments(builder, pathSegments);
			return builder;
		} catch (URISyntaxException e) {
			throw BpmError
			.create(DOCUWARE_ERROR + "invalidurlformat")
			.withCause(e)
			.withMessage("Could not convert '%s' to a valid integration URL for configKey '%s' and path segments: %s".formatted(cfg.getUrl(), configKey, String.join(", ", pathSegments)))
			.build();
		}

	}

	/**
	 * Convenience function to build URLs.
	 * 
	 * @param builder
	 * @param pathSegments
	 * @return
	 */
	protected URIBuilder addPathSegments(URIBuilder builder, String...pathSegments) {
		List<String> segs = new ArrayList<>(builder.getPathSegments());

		for (String pathSegment : pathSegments) {
			if(StringUtils.isNotBlank(pathSegment)) {
				segs.add(pathSegment);
			}
		}

		builder.setPathSegments(segs);

		return builder;
	}


	/**
	 * Get the URL of a viewer usable for embedding. 
	 * 
	 * @param configKey
	 * @param organizationName
	 * @param loginToken
	 * @param cabinetId
	 * @param documentId
	 * @return
	 */
	public String getViewerUrl(String configKey, String organizationName, String loginToken, String cabinetId, String documentId) {
		var url = getIntegrationUrl(configKey, organizationName);

		var params = new LinkedHashMap<String, String>();

		params.put("p", "V");

		if(StringUtils.isNotBlank(loginToken)) {
			params.put("lct", loginToken);
		}
		if(StringUtils.isNotBlank(cabinetId)) {
			params.put("fc", cabinetId);
		}
		if(StringUtils.isNotBlank(documentId)) {
			params.put("did", documentId);
		}

		var clear = params.entrySet().stream().map(e -> "%s=%s".formatted(e.getKey(), e.getValue())).collect(Collectors.joining("&"));

		var ep = dwEncrypt(configKey, clear);

		url.addParameter("ep", ep);

		return url.toString();
	}

	/**
	 * Get the URL of a viewer usable for embedding. 
	 * 
	 * @param configKey
	 * @param organizationName
	 * @param loginToken
	 * @param cabinetId
	 * @return
	 */
	public String getCabinetResultListAndViewerUrl(String configKey, String organizationName, String loginToken, String cabinetId) {
		var url = getIntegrationUrl(configKey, organizationName);

		var params = new LinkedHashMap<String, String>();

		params.put("p", "RLV");

		if(StringUtils.isNotBlank(loginToken)) {
			params.put("lct", loginToken);
		}
		if(StringUtils.isNotBlank(cabinetId)) {
			params.put("fc", cabinetId);
		}

		var clear = params.entrySet().stream().map(e -> "%s=%s".formatted(e.getKey(), e.getValue())).collect(Collectors.joining("&"));

		var ep = dwEncrypt(configKey, clear);

		url.addParameter("ep", ep);

		return url.toString();
	}


	/**
	 * Special variant of Base64 URL encoding with padding digit instead of '=' characters.
	 * 
	 * @param input
	 * @return
	 */
	public String dwUrlEncode(byte[] input) {
		if (input == null || input.length < 1) {
			return null;
		}

		var inputBase64 = Base64.getEncoder().encodeToString(input);

		var buf = new StringBuffer(inputBase64);
		var cnt = 0;
		while(buf.length() > 0 && buf.charAt(buf.length() - 1) == '=') {
			buf.deleteCharAt(buf.length() - 1);
			cnt++;
		}
		buf.append((char)('0' + cnt));

		return buf.toString().replace('+', '-').replace('/', '_');
	}

	/**
	 * Special variant of Base64 URL decoding with padding digit instead of '=' characters.
	 * 
	 * @param input
	 * @return
	 */
	public byte[] dwUrlDecode(String input) {
		if (input == null || input.length() < 1) {
			return null;
		}

		var buf = new StringBuffer(input.replace('-', '+').replace('_', '/'));

		int last = buf.length() - 1;
		var cnt = buf.charAt(last) - '0';
		buf.deleteCharAt(last);
		while(cnt-- > 0) {
			buf.append('=');
		}

		return Base64.getDecoder().decode(buf.toString());
	}




	/**
	 * Get a {@link Cipher}.
	 * 
	 * Use constants {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE} to determine the mode.
	 * 
	 * Key and Iv parameter needed for the Cipher are determined by building the SHA-512 hash of the password
	 * and taking the first 256 bits for the key and the next 128 bits for the Iv parameter.
	 * 
	 * @param configKey
	 * @param mode
	 * @return
	 */
	public Cipher getCipher(String configKey, int mode) {

		var cfg = Configuration.getKnownConfigurationOrDefault(configKey);

		Cipher cipher = null;
		var passphrase = cfg.getIntegrationPassphrase();
		if(StringUtils.isBlank(passphrase)) {
			BpmError
			.create(DOCUWARE_ERROR + "missingintegrationpassphrase")
			.withMessage("Integration Passphrase was not set.")
			.throwError();
		}

		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			var md = MessageDigest.getInstance("SHA-512");
			md.reset();

			var passphraseSHA512 = md.digest(passphrase.getBytes(StandardCharsets.UTF_8));

			// Split Hash into key and iv
			int keySize = 256 / 8;
			int ivSize = 128 / 8;
			byte[] key = Arrays.copyOfRange(passphraseSHA512, 0, keySize);
			byte[] iv = Arrays.copyOfRange(passphraseSHA512, keySize, keySize + ivSize);

			var secretKeySpec = new SecretKeySpec(key, "AES");
			var ivParameter = new IvParameterSpec(iv);

			cipher.init(mode, secretKeySpec, ivParameter);
		} catch (Exception e) {
			BpmError
			.create(DOCUWARE_ERROR + "ciphercreationerror")
			.withCause(e)
			.withMessage("Error while creating the cipher.")
			.throwError();
		}
		return cipher;
	}

	/**
	 * Encrypt a String.
	 * 
	 * @param configKey
	 * @param clear
	 * @return
	 */
	public String dwEncrypt(String configKey, String clear) {
		String encoded = null;
		var cipher = getCipher(configKey, Cipher.ENCRYPT_MODE);

		try {
			var encrypted = cipher.doFinal(clear.getBytes("UTF-8"));
			encoded = dwUrlEncode(encrypted);
		} catch (Exception e) {
			BpmError
			.create(DOCUWARE_ERROR + "encrypterror")
			.withCause(e)
			.withMessage("Error while encrypting.")
			.throwError();
		}

		return encoded;
	}

	/**
	 * Decrypt a String.
	 * 
	 * @param configKey
	 * @param encrypted
	 * @return
	 */
	public String dwDecrypt(String configKey, String encrypted) {
		String decrypted = null;

		var cipher = getCipher(configKey, Cipher.DECRYPT_MODE);

		try {
			var decoded = dwUrlDecode(encrypted);
			var decryptedBytes = cipher.doFinal(decoded);
			decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			BpmError
			.create(DOCUWARE_ERROR + "decrypterror")
			.withCause(e)
			.withMessage("Error while decrypting.")
			.throwError();
		}

		return decrypted;
	}


	/**
	 * Get the DocuWare user to use based on the current Ivy user.
	 * 
	 * This is useful for trusted grant type, where this will be used for the impersonateName.
	 * Normal Ivy users will be used with the user-name. Unknown (unauthenticated), system
	 * or developer user will be matched to the fallback username. 
	 * 
	 * @return
	 */
	public String getDocuwareUserBasedOnCurrentUser() {
		var session = Ivy.session();

		String username = null;

		if (session.isSessionUserSystemUser() || session.isSessionUserUnknown()) {
			username = getIvyVar(DocuWareVariable.USERNAME);
		}
		else {
			username = session.getSessionUserName();
			if(ISecurityConstants.DEVELOPER_USER_NAME.equals(username)) {
				username = getIvyVar(DocuWareVariable.USERNAME);
			}
		}
		return username;
	}


	private String createTokenCacheKey(String configKey, String extra) {
		var key = "%s:%s".formatted(APP_ATT_TOKEN_PREFIX, Configuration.knownOrDefaultKey(configKey));
		if(extra != null) {
			key = "%s:%s".formatted(key, extra);
		}
		return key;
	}

	/**
	 * Get a LoginToken based on the current access token.
	 * 
	 * @param configKey
	 * @return
	 */
	public String getLoginTokenString(String configKey) {
		return getLoginTokenString(configKey, null);
	}

	/**
	 * Get a LoginToken based on the access token.
	 * 
	 * @param configKey
	 * @param token
	 * @return
	 */
	public String getLoginTokenString(String configKey, Token token) {
		String loginToken = null;
		Response response = null;
		try {
			response = getLoginTokenResponse(configKey, token);

			if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
				loginToken = response.readEntity(String.class);
			}
		} catch (Exception e) {
			BpmError.create(DOCUWARE_ERROR + "logintoken")
			.withCause(e)
			.withMessage("Could not get login token.")
			.throwError();
		}
		return loginToken;
	}

	/**
	 * Get a LoginToken based on the current access token.
	 * 
	 * @param configKey
	 * @return
	 */
	public Response getLoginTokenResponse(String configKey) {
		return getLoginTokenResponse(configKey, null);
	}
	/**
	 * Get a LoginToken based on the access token.
	 * 
	 * @param configKey
	 * @param token
	 * @return
	 */
	public Response getLoginTokenResponse(String configKey, Token token) {
		var client = getClient(configKey);
		Response response = null;
		try {
			response = client
					.path("Organization/LoginToken")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.json(generateLoginTokenBody()));

		} catch (Exception e) {
			BpmError.create(DOCUWARE_ERROR + "logintoken")
			.withCause(e)
			.withMessage("Could not get login token.")
			.throwError();
		}
		return response;
	}

	private String generateLoginTokenBody() {
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

	public JsonNode getWebTargetResponseAsJsonNode(URI targetURI) {
		Client client = ClientBuilder.newClient();
		Response response = null;
		try {
			WebTarget target = client.target(targetURI);
			response = target.request(MediaType.APPLICATION_JSON).get();
			if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
				String jsonResponse = response.readEntity(String.class);
				return parseToJsonNode(jsonResponse);
			}
		} catch (Exception e) {
			Ivy.log().error("Error calling URL ''{0}'': status is {1} - {2}", e, targetURI, response != null ? response.getStatus() : null, response);
		} finally {
			if(response != null) {
				response.close();
			}
			if(client != null) {
				client.close();
			}
		}
		return null;
	}

	public DocumentIndexField createDocumentIndexStringField(String fieldName, String item) {
		DocumentIndexField field = new DocumentIndexField();
		field.setFieldName(fieldName);
		field.setString(item);
		return field;
	}

	public DocumentIndexField createDocumentIndexDateField(String fieldName, Date date) {
		DocumentIndexField field = new DocumentIndexField();
		field.setFieldName(fieldName);
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		// set
		return field;
	}

	public String dateToString(Date date) {
		String string = null;
		if (date != null) {
			string = String.format("/Date(%d)/", date.getTime());
		}
		return string;
	}

	public Date stringToDate(String dateString) {
		Date date = null;
		if (dateString != null) {
			Matcher matcher = DATE_PATTERN.matcher(dateString);
			if (matcher.matches()) {
				long timestamp = Long.parseLong(matcher.group(1));
				date = new Date(timestamp);
			}
		}
		return date;
	}

	/**
	 * Upload a document.
	 * 
	 * @param target
	 * @param fileStream
	 * @param fileName
	 * @param properties
	 * @return
	 * @throws IOException
	 * @throws DocuWareException
	 */
	public Document upload(WebTarget target, InputStream fileStream, String fileName, DocuWareProperties properties)
			throws IOException, DocuWareException {
		var propertiesStream = new ByteArrayInputStream(writeObjectAsJsonBytes(properties));
		Document document = null;

		try (var multiPart = new FormDataMultiPart()) {
			multiPart
			.bodyPart(new StreamDataBodyPart(PROPERTIES_FILE_NAME, propertiesStream, "Properties.json", MediaType.APPLICATION_JSON_TYPE))
			.bodyPart(new StreamDataBodyPart("File[]", fileStream, fileName));

			var response = prepareRestClient(target).post(Entity.entity(multiPart, multiPart.getMediaType()));

			if (Status.Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
				document = response.readEntity(Document.class);
			} else {
				throw handleError(response);
			}
			response.close();
		}
		return document;
	}

	public Document checkInFromFileSystem(WebTarget target, DocuWareCheckInActionParameters params, String fileName, InputStream file) throws IOException {
		Document document = null;

		var checkIn = new ByteArrayInputStream(writeObjectAsJsonBytes(params));

		try (var multiPart = new FormDataMultiPart()) {
			multiPart
			.bodyPart(new StreamDataBodyPart("CheckIn", checkIn, "CheckIn.json", MediaType.APPLICATION_JSON_TYPE))
			.bodyPart(new StreamDataBodyPart("File[]", file, fileName));

			var response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()));
			if (Status.Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
				document = response.readEntity(Document.class);
			} else {
				BpmError.create(DOCUWARE_ERROR + "checkin").build();
			}
		} 
		return document;
	}

	/**
	 * Create check-in parameters.
	 * 
	 * @param checkInReturnDocument
	 * @param comments
	 * @param major
	 * @param minor
	 * @return
	 */
	public DocuWareCheckInActionParameters createCheckInActionParameters(CheckInReturnDocument checkInReturnDocument, String comments, int major, int minor) {
		var params = new DocuWareCheckInActionParameters();
		params.setCheckInReturnDocument(checkInReturnDocument);
		params.setComments(comments);
		var version = new DocuWareDocumentVersion();
		version.setMajor(major);
		version.setMinor(minor);
		params.setDocumentVersion(version);

		return params;
	}



	public DocuWareException handleError(Response response) {
		String errXml = response.readEntity(String.class);
		String httpStatus = String.valueOf(response.getStatus());
		String msg = "DocuWare Service call failed";
		DocuWareException exception = new DocuWareException(msg, httpStatus);
		try {
			InputStream isr = new ByteArrayInputStream(errXml.getBytes(StandardCharsets.UTF_8));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document doc = db.parse(isr);
			Element element = doc.getDocumentElement();
			if (element != null && element.getNodeName() != null
					&& element.getNodeName().contains(RESPONSE_XML_ERROR_NODE)) {
				for (int n = 0; n < element.getChildNodes().getLength(); n++) {
					Node node = element.getChildNodes().item(n);
					if (node.getNodeName() != null && node.getNodeName().contains(RESPONSE_XML_MESSAGE_NODE)) {
						if (node.getChildNodes() != null) {
							Node child = node.getFirstChild();
							msg = child.getNodeValue();
							exception = new DocuWareException(msg, httpStatus);
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return exception;
	}

	public String getFilenameFromResponseHeader(Response response) {
		String filename = null;
		if (response != null) {
			String disposition = response.getHeaderString(CONTENT_DISPOSITION);

			try {
				var cd = new ContentDisposition(disposition);
				filename = cd.getFileName(true);
			} catch (ParseException e) {
				BpmError
				.create(DOCUWARE_ERROR + "invalidfilename")
				.withMessage("Could not extract filename of %s header value '%s'".formatted(CONTENT_DISPOSITION, disposition))
				.throwError();
			}
		}
		return filename;
	}

	public File createPropertiesFile(List<DocuWareProperty> properties) throws IOException {
		File propertiesFile = getUniquePropertiesFile();
		DocuWareProperties docuWareproperties = new DocuWareProperties();
		docuWareproperties.setProperties(properties);
		FileUtils.write(propertiesFile.getJavaFile(), serializeProperties(docuWareproperties),
				PROPERTIES_FILE_CHARSET);
		return propertiesFile;
	}

	protected Builder prepareRestClient(WebTarget target) {
		return target.request()
				.header("X-Requested-By", "ivy")
				.header("MIME-Version", "1.0")
				.header("Accept", "application/xml");
	}

	protected File getUniquePropertiesFile() throws IOException {
		return new File(PROPERTIES_FILE_NAME + UUID.randomUUID().toString() + PROPERTIES_FILE_EXTENSION, true);
	}

	/**
	 * Serializes {@link DocuWareProperties} to {@link String}
	 *
	 * @param properties
	 * @return
	 * @throws JsonProcessingException
	 */
	public String serializeProperties(DocuWareProperties properties) throws JsonProcessingException {
		return getObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(properties);
	}

	/**
	 * Serializes {@link DocuWarePropertiesUpdate} to {@link String}
	 *
	 * @param properties
	 * @return
	 * @throws JsonProcessingException
	 */
	public String serializeProperties(DocuWarePropertiesUpdate properties) throws JsonProcessingException {
		return getObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(properties);
	}

	/**
	 * Registers the timeModule within the class loader
	 *
	 * @return
	 */
	protected Module timeModule() {
		try {
			return (Module) StringUtil.class.getClassLoader()
					.loadClass("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule").getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException("JSR time module not available", e);
		}
	}

	public String writeObjectAsJson(Object entity) {
		try {
			return getObjectMapper().writeValueAsString(entity);
		} catch (JsonProcessingException e) {
			Ivy.log().warn(e.getMessage());
		}
		return null;
	}

	public byte[] writeObjectAsJsonBytes(Object entity) {
		try {
			return getObjectMapper().writeValueAsBytes(entity);
		} catch (JsonProcessingException e) {
			Ivy.log().warn(e.getMessage());
		}
		return null;
	}

	public <T> T convertJsonToObject(String json, Class<T> objectType) {
		if (StringUtils.isEmpty(json)) {
			return null;
		}
		try {
			return getObjectMapper().readValue(json, objectType);
		} catch (JsonProcessingException e) {
			Ivy.log().warn(e.getMessage());
		}
		return null;
	}

	public ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Module timeModule = timeModule();
			objectMapper.registerModule(timeModule);
			objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
			objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
			objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			objectMapper.setSerializationInclusion(Include.NON_NULL);
		}
		return objectMapper;
	}


	protected JsonNode parseToJsonNode(String value) {
		try {
			var jsonNode = getObjectMapper().readTree(value);
			Ivy.log().info("JSON Response: " + jsonNode.toPrettyString());
			return jsonNode;
		} catch (Exception e) {
			Ivy.log().error(e);
		}
		return null;
	}
}
