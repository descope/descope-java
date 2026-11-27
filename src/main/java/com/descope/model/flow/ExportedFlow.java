package com.descope.model.flow;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A flow and its screens, as exported by and imported to /v2/mgmt/flow. */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExportedFlow {
  private String flowId;
  private ExportedFlowMetadata metadata;
  private Map<String, Object> contents;
  private List<ExportedScreen> screens;
  private ProjectReferences references;
}
