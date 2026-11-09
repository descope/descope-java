package com.descope.model.flow;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowMetadata {
  private String id;
  private String name;
  private String description;
  private boolean disabled;
  private Integer version;
  private Object dsl;
  private Long modifiedTime;
  private String etag;
  private Boolean translate;
  private String translateConnectorID;
  private String translateSourceLang;
  private List<String> translateTargetLangs;
  private Boolean fingerprint;
  private Boolean widget;
  private List<Interaction> sharedInteractions;
  private Boolean management;
  private ManagementConfig managementConfig;
  private Boolean referencesGlobalStrings;
  private Boolean clientValidationTracking;

  // Keeps the pre-existing all-args constructor for binary compatibility.
  public FlowMetadata(String id, String name, String description, boolean disabled) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.disabled = disabled;
  }
}
