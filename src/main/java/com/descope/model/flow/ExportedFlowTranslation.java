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
public class ExportedFlowTranslation {
  private Boolean enabled;
  private String connectorId;
  private String sourceLanguage;
  private List<String> targetLanguages;
}
