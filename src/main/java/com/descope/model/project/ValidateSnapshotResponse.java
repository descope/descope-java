package com.descope.model.project;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateSnapshotResponse {
  /** True when the snapshot can be imported, with no failures. */
  private boolean ok;
  private List<String> failures;
  /** Secrets the import needs that weren't provided, listed without a value. */
  private SnapshotSecrets missingSecrets;
}
