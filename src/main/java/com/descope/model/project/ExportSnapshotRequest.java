package com.descope.model.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportSnapshotRequest {
  /** The export format, leave unset for the default. */
  private String format;
}
