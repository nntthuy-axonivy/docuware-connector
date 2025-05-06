package com.axonivy.connector.docuware.connector.demo.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.axonivy.connector.docuware.connector.DocuWareConstants;
import com.axonivy.connector.docuware.connector.DocuWareEndpointConfiguration;
import com.axonivy.connector.docuware.connector.DocuWareProperty;
import com.axonivy.connector.docuware.connector.DocuWareService;
import com.axonivy.connector.docuware.connector.bo.DocuWareDocument;
import com.axonivy.connector.docuware.connector.bo.DocuWareInstance;
import com.axonivy.connector.docuware.connector.demo.enums.ItemType;
import com.axonivy.connector.docuware.connector.enums.DocuWareVariable;
import com.axonivy.connector.docuware.connector.utils.DocuWareUtils;
import com.docuware.dev.schema._public.services.platform.FileCabinet;
import com.docuware.dev.schema._public.services.platform.Organization;
import com.docuware.dev.schema._public.services.platform.Organizations;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.call.SubProcessCall;
import ch.ivyteam.ivy.process.call.SubProcessCallResult;

@ManagedBean
@ViewScoped
public class DocuWareDemoBean implements Serializable {
  private static final long serialVersionUID = 4548574141754643263L;

  private String documentUrl;
  private DocuWareInstance selectedInstance;
  private List<DocuWareInstance> availableInstances;
  private List<Organization> selectedOrganizations;
  private List<Organization> availableOrganizations;
  private List<FileCabinet> selectedCabinets;
  private List<FileCabinet> availableCabinets;
  private List<DocuWareDocument> docuWareDocuments;

  @PostConstruct
  public void initData() {
    availableInstances = DocuWareService.get().collectAvailableIntances();
    selectedOrganizations = new ArrayList<>();
    availableOrganizations = new ArrayList<>();
    selectedCabinets = new ArrayList<>();
    availableCabinets = new ArrayList<>();
    docuWareDocuments = new ArrayList<>();
  }

  public void buildDocumentUrl(DocuWareDocument document) {
    String host = DocuWareUtils.getIvyVar(DocuWareVariable.HOST);
    this.documentUrl = String.format(DocuWareConstants.VIEW_DOCUMENT_URL_FORMAT, host, document.getId(),
        document.getFileCabinetId());
  }

  public void onChangeInstance() {
    if (selectedInstance == null) {
      return;
    }
    // Reset all data
    selectedOrganizations.clear();
    availableOrganizations.clear();
    selectedCabinets.clear();
    availableCabinets.clear();
    docuWareDocuments.clear();
    // Init configuration for selected instance
    DocuWareService.get().initializeConfigurationForInstance(selectedInstance.getInstance());
    // Re-load organizations
    availableOrganizations.addAll(findAllOrganzations());
  }

  public List<Organization> completeOrganization(String keyword) {
    return findAllOrganzations().stream().filter(org -> org.getName().contains(keyword)).toList();
  }

  public List<FileCabinet> completeFileCabinet(String keyword) {
    return findAllFileCabinetsByOrg().stream().filter(cabinet -> cabinet.getName().contains(keyword)).toList();
  }

  @SuppressWarnings("unchecked")
  public List<DocuWareDocument> queryDocuments() {
    DocuWareEndpointConfiguration configuration = new DocuWareEndpointConfiguration();
    configuration.getFileCabinetIds().addAll(selectedCabinets.stream().map(FileCabinet::getId).toList());

    SubProcessCallResult callResult = SubProcessCall.withPath("FileCabinetService")
        .withStartSignature("queryDocument()").call(configuration);
    return callResult.get("docuWareDocuments", List.class);
  }
  
  public void queryFileCabinetByOrgs() {
    selectedCabinets.clear();
    availableCabinets.clear();
    availableCabinets.addAll(findAllFileCabinetsByOrg());
  }

  @SuppressWarnings("unchecked")
  private List<FileCabinet> findAllFileCabinetsByOrg() {
    List<String> orgIds = selectedOrganizations.stream().map(Organization::getId).collect(Collectors.toList());
    ch.ivyteam.ivy.scripting.objects.List<String> selectedOrgIds = new ch.ivyteam.ivy.scripting.objects.List<>();
    selectedOrgIds.addAll(orgIds);
    SubProcessCallResult callResult = SubProcessCall.withPath("FileCabinetService")
        .withStartName("findAllByMultiOrgs").call(orgIds);
    return callResult.get("fileCabinets", List.class);
  }

  private List<Organization> findAllOrganzations() {
    SubProcessCallResult callResult = SubProcessCall.withPath("OrganizationService")
        .withStartSignature("findAll()").call();
    Organizations organizations = callResult.get("organizations", Organizations.class);
    return Optional.ofNullable(organizations).map(Organizations::getOrganization).orElse(List.of());
  }

  public boolean isFieldNameTypeNumber(DocuWareProperty field) {
    return field.getItemElementName().equals(ItemType.DECIMAL.getValue())
        || field.getItemElementName().equals(ItemType.INT.getValue());
  }

  public boolean isFieldNameTypeString(DocuWareProperty field) {
    return field.getItemElementName().equals(ItemType.STRING.getValue());
  }

  public void updateGrowlMessageForEditAction(boolean isUpdateSuccess) {
    FacesContext.getCurrentInstance()
      .addMessage(null, new FacesMessage(isUpdateSuccess ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
            Ivy.cms().co("/Labels/" + (isUpdateSuccess ? "Success" : "Failed")),
            Ivy.cms().co("/Dialogs/com/axonivy/connector/docuware/connector/demo/DocuWareDemoUI/Dialog/"
                + (isUpdateSuccess ? "SuccessEditMessage" : "FailedEditMessage"))));
  }

  public String getDocumentUrl() {
    return documentUrl;
  }

  public DocuWareInstance getSelectedInstance() {
    return selectedInstance;
  }

  public void setSelectedInstance(DocuWareInstance selectedInstance) {
    this.selectedInstance = selectedInstance;
  }

  public List<DocuWareInstance> getAvailableInstances() {
    return availableInstances;
  }

  public void setAvailableInstances(List<DocuWareInstance> availableInstances) {
    this.availableInstances = availableInstances;
  }

  public List<Organization> getSelectedOrganizations() {
    return selectedOrganizations;
  }

  public void setSelectedOrganizations(List<Organization> selectedOrganizations) {
    this.selectedOrganizations = selectedOrganizations;
  }

  public List<Organization> getAvailableOrganizations() {
    return availableOrganizations;
  }

  public void setAvailableOrganizations(List<Organization> availableOrganizations) {
    this.availableOrganizations = availableOrganizations;
  }

  public List<FileCabinet> getSelectedCabinets() {
    return selectedCabinets;
  }

  public void setSelectedCabinets(List<FileCabinet> selectedCabinets) {
    this.selectedCabinets = selectedCabinets;
  }

  public List<FileCabinet> getAvailableCabinets() {
    return availableCabinets;
  }

  public void setAvailableCabinets(List<FileCabinet> availableCabinets) {
    this.availableCabinets = availableCabinets;
  }

  public List<DocuWareDocument> getDocuWareDocuments() {
    return docuWareDocuments;
  }

  public void setDocuWareDocuments(List<DocuWareDocument> docuWareDocuments) {
    this.docuWareDocuments = docuWareDocuments;
  }

  public void setDocumentUrl(String documentUrl) {
    this.documentUrl = documentUrl;
  }
}
