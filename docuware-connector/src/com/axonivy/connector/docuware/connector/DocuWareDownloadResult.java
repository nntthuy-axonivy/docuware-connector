package com.axonivy.connector.docuware.connector;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.scripting.objects.File;

public class DocuWareDownloadResult {

  File file;
  BpmError error;

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public BpmError getError() {
    return error;
  }

  public void setError(BpmError error) {
    this.error = error;
  }
}
