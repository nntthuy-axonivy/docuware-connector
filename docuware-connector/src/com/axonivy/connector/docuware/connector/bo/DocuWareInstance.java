package com.axonivy.connector.docuware.connector.bo;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.ivyteam.ivy.environment.Ivy;

public class DocuWareInstance {
  private String instance;
  private String host;
  private boolean isActivated;

  public DocuWareInstance() {
    super();
  }

  public DocuWareInstance(String instance) {
    super();
    this.instance = instance;
  }

  public String getDisplayName() {
    String noHost = Ivy.cms().co("/Labels/NoHost");
    String hostDisplayName = StringUtils.isBlank(host) ? noHost : host;
    return Ivy.cms().co(isActivated ? "/Labels/ActiveInstanceDisplayNamePattern" : "/Labels/InstanceDisplayNamePattern",
        Arrays.asList(instance, hostDisplayName));
  }

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public boolean isActivated() {
    return isActivated;
  }

  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DocuWareInstance)) {
      return false;
    }
    DocuWareInstance other = (DocuWareInstance) obj;
    EqualsBuilder builder = new EqualsBuilder();
    builder.append(instance, other.getInstance());
    return builder.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(instance);
    return builder.hashCode();
  }

}
