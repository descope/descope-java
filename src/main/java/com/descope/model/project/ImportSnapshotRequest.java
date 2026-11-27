package com.descope.model.project;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSnapshotRequest {
  /** Required: the files returned by a snapshot export. */
  private Map<String, Object> files;
  /** Parts of the snapshot to skip, for example {@code "lists"}. */
  private List<String> excludes;
  /** Secrets to set in the project, which a snapshot export does not include. */
  private SnapshotSecrets inputSecrets;
}
