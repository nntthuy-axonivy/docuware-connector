package com.axonivy.connector.docuware.connector.utils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.docuware.connector.DocuWareProperties;
import com.axonivy.connector.docuware.connector.DocuWarePropertiesUpdate;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.util.StringUtil;

/**
 * Provides utilities related to json serialization like building an
 * ObjectMapper with all required options and features.
 *
 * @author jpl
 *
 */
public class JsonUtils {

  private static ObjectMapper objectMapper;

  /**
   * Serializes {@link DocuWareProperties} to {@link String}
   *
   * @param properties
   * @return
   * @throws JsonProcessingException
   */
  public static String serializeProperties(DocuWareProperties properties) throws JsonProcessingException {
    return getObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(properties);
  }

  /**
   * Serializes {@link DocuWarePropertiesUpdate} to {@link String}
   *
   * @param properties
   * @return
   * @throws JsonProcessingException
   */
  public static String serializeProperties(DocuWarePropertiesUpdate properties) throws JsonProcessingException {
    return getObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(properties);
  }

  /**
   * Registers the timeModule within the class loader
   *
   * @return
   */
  private static Module timeModule() {
    try {
      return (Module) StringUtil.class.getClassLoader()
          .loadClass("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule").getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
      throw new RuntimeException("JSR time module not available", e);
    }
  }

  public static String writeObjectAsJson(Object entity) {
    try {
      return getObjectMapper().writeValueAsString(entity);
    } catch (JsonProcessingException e) {
      Ivy.log().warn(e.getMessage());
    }
    return null;
  }

  public static <T> T convertJsonToObject(String json, Class<T> objectType) {
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

  public static ObjectMapper getObjectMapper() {
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


  public static JsonNode parseToJsonNode(String value) {
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
