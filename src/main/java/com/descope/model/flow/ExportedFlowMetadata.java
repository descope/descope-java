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
public class ExportedFlowMetadata {
  private String name;
  private String description;
  private String componentsVersion;
  private Boolean disabled;
  private Boolean fingerprint;
  private Boolean widget;
  private ExportedFlowTranslation translation;
  private List<Interaction> sharedInteractions;
  private Boolean management;
  private ManagementConfig managementConfig;
  private Boolean clientValidationTracking;
}
