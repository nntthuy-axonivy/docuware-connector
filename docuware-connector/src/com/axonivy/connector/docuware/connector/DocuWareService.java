package com.axonivy.connector.docuware.connector;

import static com.axonivy.connector.docuware.connector.DocuWareConstants.ARCHIVED_INSTANCE;
import static com.axonivy.connector.docuware.connector.DocuWareConstants.SEMICOLON;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.ACCESS_TOKEN;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.CONNECT_TIMEOUT;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.DEFAULT_INSTANCE;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.FILE_CABINET_ID;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.GRANT_TYPE;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.HOST;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.LOGIN_TOKEN;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.PASSWORD;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.STORE_DIALOG_ID;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.TRUSTED_USERNAME;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.TRUSTED_USER_PASSWORD;
import static com.axonivy.connector.docuware.connector.enums.DocuWareVariable.USERNAME;
import static com.axonivy.connector.docuware.connector.utils.DocuWareUtils.getIvyVar;
import static com.axonivy.connector.docuware.connector.utils.DocuWareUtils.setIvyVar;
import static com.axonivy.connector.docuware.connector.utils.DocuWareUtils.setVariableByInstance;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.Boundary;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.axonivy.connector.docuware.connector.bo.DocuWareInstance;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.axonivy.connector.docuware.connector.utils.DocuWareUtils;
import com.axonivy.connector.docuware.connector.utils.JsonUtils;
import com.docuware.dev.schema._public.services.platform.Document;
import com.docuware.dev.schema._public.services.platform.DocumentIndexField;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.vars.Variables;

public class DocuWareService {

/*
   * This is the format: /Date(1652285631000)/
   */
  private static final Pattern DATE_PATTERN = Pattern.compile("/Date\\(([0-9]+)\\)/");
  private static final String PROPERTIES_FILE_NAME = "document";
  private static final String PROPERTIES_FILE_EXTENSION = ".json";
  private static final String PROPERTIES_FILE_CHARSET = "UTF-8";
  private static final String FILE_NAME_DISPOSITION_REGEX = "(?i)filename\\*?=\"?([^\";]+)"; // Try to extract filename* (RFC 5987) first, then fallback to filename=
  private static final Pattern FILE_NAME_DISPOSITION_PATTERN = Pattern.compile(FILE_NAME_DISPOSITION_REGEX);
  private static final String CONTENT_DISPOSITION = "Content-Disposition";
  private static final String RESPONSE_XML_ERROR_NODE = "Error";
  private static final String RESPONSE_XML_MESSAGE_NODE = "Message";
  private static final String STORE_DIALOG_ID_PARAM = "storeDialogId";
  private static final String VAR_SEPERATOR = "\\.";
  private static final DocuWareService INSTANCE = new DocuWareService();

  public static DocuWareService get() {
    return INSTANCE;
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

  public static Document uploadFile(WebTarget target, java.io.File file,
          DocuWareEndpointConfiguration configuration,
          List<DocuWareProperty> properties) throws IOException, DocuWareException {
    byte[] bytes = Files.readAllBytes(file.toPath());
    return uploadStream(target, bytes, file.getName(), configuration, properties);
  }

  public static Document uploadStream(WebTarget target, ch.ivyteam.ivy.scripting.objects.List<Byte> fileBytes,
          String fileName, DocuWareEndpointConfiguration configuration, List<DocuWareProperty> properties)
          throws IOException, DocuWareException {
    Byte[] bytes = fileBytes.toArray(new Byte[fileBytes.size()]);
    byte[] byteArray = ArrayUtils.toPrimitive(bytes);
    return uploadStream(target, byteArray, fileName, configuration, properties);
  }

  public static Document uploadStream(WebTarget target, byte[] file, String fileName,
          DocuWareEndpointConfiguration configuration, List<DocuWareProperty> properties)
          throws IOException, DocuWareException {
    FormDataMultiPart multipart;
    File propertiesFile = createPropertiesFile(properties);
    try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
      InputStream streamProperties = new FileInputStream(propertiesFile.getJavaFile());
      StreamDataBodyPart streamPropertiesPart = new StreamDataBodyPart(PROPERTIES_FILE_NAME,
              streamProperties);
      streamPropertiesPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
      InputStream stream = new ByteArrayInputStream(file);
      StreamDataBodyPart streamPart = new StreamDataBodyPart(fileName, stream);
      multipart = (FormDataMultiPart) formDataMultiPart.bodyPart(streamPropertiesPart);
      multipart.bodyPart(streamPart);
    }
    MediaType contentType = MediaType.MULTIPART_FORM_DATA_TYPE;
    contentType = Boundary.addBoundary(contentType);
    if(StringUtils.isNotBlank(configuration.getStoreDialogId())) {
  	  target = target.queryParam(STORE_DIALOG_ID_PARAM, configuration.getStoreDialogId());
  	}
    Response response = prepareRestClient(target, configuration).post(Entity.entity(multipart, contentType));
    FileUtils.forceDelete(propertiesFile.getJavaFile());
    Document document = null;
    if (Status.Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
      document = response.readEntity(Document.class);
    } else {
      DocuWareException exception = handleError(response);
      throw exception;
    }
    response.close();
    return document;
  }

  public static DocuWareException handleError(Response response) {
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

  public static String getFilenameFromResponseHeader(Response response) {
    String filename = null;
    if (response != null) {
      String disposition = response.getHeaderString(CONTENT_DISPOSITION);
      Matcher matcher = FILE_NAME_DISPOSITION_PATTERN.matcher(disposition);
      if (matcher.find()) {
        filename = matcher.group(1);
      }
    }
    return filename;
  }

  public static File createPropertiesFile(List<DocuWareProperty> properties) throws IOException {
    File propertiesFile = getUniquePropertiesFile();
    DocuWareProperties docuWareproperties = new DocuWareProperties();
    docuWareproperties.setProperties(properties);
    FileUtils.write(propertiesFile.getJavaFile(), JsonUtils.serializeProperties(docuWareproperties),
            PROPERTIES_FILE_CHARSET);
    return propertiesFile;
  }

  private static Builder prepareRestClient(WebTarget target, DocuWareEndpointConfiguration configuration) {
    return target.request().header("X-Requested-By", "ivy").header("MIME-Version", "1.0")
            .header("Accept", "application/xml").header("Connection", "keep-alive");
  }

  private static File getUniquePropertiesFile() throws IOException {
    return new File(PROPERTIES_FILE_NAME + UUID.randomUUID().toString() + PROPERTIES_FILE_EXTENSION, true);
  }

  public static DocuWareEndpointConfiguration initializeDefaultConfiguration() {
    DocuWareEndpointConfiguration config = DocuWareUtils.getDefaultActiveInstance();
    if (config == null) {
      unifyConfigurationByInstance();
      config = DocuWareUtils.getDefaultActiveInstance();
    }
    var fileCabinetId = config.getFileCabinetId();
    if (StringUtils.contains(fileCabinetId, SEMICOLON)) {
      config.setFileCabinetIds(Arrays.asList(fileCabinetId.split(SEMICOLON)));
      config.setFileCabinetId(config.getFileCabinetIds().get(0));
    } else {
      config.setFileCabinetId(fileCabinetId);
      config.setFileCabinetIds(new ArrayList<>());
    }
    config.setStoreDialogId(Ivy.var().get(DocuWareVariable.STORE_DIALOG_ID.getVariableName()));
    return config;
  }

  public static DocuWareEndpointConfiguration initializeConfiguration(DocuWareEndpointConfiguration config) {
    DocuWareEndpointConfiguration defaultConfig = initializeDefaultConfiguration();
    if (StringUtils.isBlank(config.getFileCabinetId())) {
      config.setFileCabinetId(defaultConfig.getFileCabinetId());
    }
    if (CollectionUtils.isEmpty(config.getFileCabinetIds())) {
      config.setFileCabinetIds(defaultConfig.getFileCabinetIds());
    }
    config.setFileCabinetIds(config.getFileCabinetIds().stream().filter(StringUtils::isNoneBlank).distinct().toList());

    if (StringUtils.isBlank(config.getStoreDialogId())) {
      config.setStoreDialogId(defaultConfig.getStoreDialogId());
    }
    return config;
  }

  public List<DocuWareInstance> collectAvailableIntances() {
    final int INSTANCE_NAME_INDEX = 2;
    List<DocuWareInstance> instances = new ArrayList<>();
    String defaultActiveInstance = Ivy.var().get(DEFAULT_INSTANCE.getVariableName());
    Variables.current().all().stream()
        .filter(var -> var.name().startsWith(DocuWareConstants.INSTANCE_PATTERN))
        .forEach(variable -> {
          // The expected value should be docuwareConnector.instances.key
          var variableNameArrays = variable.name().split(VAR_SEPERATOR);
          if (variableNameArrays.length > INSTANCE_NAME_INDEX) {
            String instanceName = variableNameArrays[INSTANCE_NAME_INDEX];
            if (instances.stream().noneMatch(instance -> instance.getInstance().equals(instanceName))) {
              DocuWareInstance docuWareInstance = new DocuWareInstance(instanceName);
              String host = DocuWareUtils.getVariableValueByInstance(instanceName, HOST);
              docuWareInstance.setHost(host);
              docuWareInstance.setActivated(instanceName.equals(defaultActiveInstance));
              instances.add(docuWareInstance);
            }
          }
        });
    return instances.stream().distinct().toList();
  }

  public DocuWareEndpointConfiguration defaultInstance() {
    return DocuWareUtils.getDefaultActiveInstance();
  }

  public DocuWareEndpointConfiguration initializeConfigurationForInstance(String instanceName) {
    DocuWareEndpointConfiguration config = DocuWareUtils.extractVariableByInstanceName(instanceName);
    String currentDefaultInstance = getIvyVar(DEFAULT_INSTANCE);
    if (StringUtils.isNoneBlank(config.getInstance()) && !config.getInstance().equals(currentDefaultInstance)) {
      Ivy.log().warn("DocuWareService: Changed DefaultInstance from {0} to {1}", currentDefaultInstance, config.getInstance());
      setIvyVar(DEFAULT_INSTANCE, config.getInstance());
      setIvyVar(HOST, config.getHost());
    }
    return config;
  }

  /**
   * This method unify the old variable to default instance
   */
  @SuppressWarnings("deprecation")
  public static void unifyConfigurationByInstance() {
    if (StringUtils.isBlank(Ivy.var().get(DEFAULT_INSTANCE.getVariableName()))) {
      // For old structure, try to collect all variables and backup to archivedInstance
      // Then clean up it, only keep the host and connectTimeout due to RestClient properties
      setIvyVar(DEFAULT_INSTANCE, ARCHIVED_INSTANCE);
      setVariableByInstance(ARCHIVED_INSTANCE, HOST, getIvyVar(HOST));
      setVariableByInstance(ARCHIVED_INSTANCE, CONNECT_TIMEOUT, getIvyVar(CONNECT_TIMEOUT));

      // Backup to archivedInstance then remove configuration
      String archivedGrantType = getIvyVar(GRANT_TYPE);
      setVariableByInstance(ARCHIVED_INSTANCE, GRANT_TYPE, archivedGrantType);
      setIvyVar(GRANT_TYPE, null);

      setVariableByInstance(ARCHIVED_INSTANCE, USERNAME, getIvyVar(USERNAME));
      setIvyVar(USERNAME, null);

      setVariableByInstance(ARCHIVED_INSTANCE, PASSWORD, getIvyVar(PASSWORD));
      setIvyVar(PASSWORD, null);

      setVariableByInstance(ARCHIVED_INSTANCE, TRUSTED_USERNAME, getIvyVar(TRUSTED_USERNAME));
      setIvyVar(TRUSTED_USERNAME, null);

      setVariableByInstance(ARCHIVED_INSTANCE, TRUSTED_USER_PASSWORD, getIvyVar(TRUSTED_USER_PASSWORD));
      setIvyVar(TRUSTED_USER_PASSWORD, null);

      setVariableByInstance(ARCHIVED_INSTANCE, ACCESS_TOKEN, getIvyVar(ACCESS_TOKEN));
      setIvyVar(ACCESS_TOKEN, null);

      setVariableByInstance(ARCHIVED_INSTANCE, LOGIN_TOKEN, getIvyVar(LOGIN_TOKEN));
      setIvyVar(LOGIN_TOKEN, null);

      setVariableByInstance(ARCHIVED_INSTANCE, FILE_CABINET_ID, getIvyVar(FILE_CABINET_ID));
      setIvyVar(FILE_CABINET_ID, null);

      setVariableByInstance(ARCHIVED_INSTANCE, STORE_DIALOG_ID, getIvyVar(STORE_DIALOG_ID));
      setIvyVar(STORE_DIALOG_ID, null);

      // Obsoleted variable
      setIvyVar(DocuWareVariable.HOST_ID, null);
      setIvyVar(DocuWareVariable.LOGON_URL, null);
    }
  }

}
