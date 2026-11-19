package com.descope.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOptions {
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  /** Sent by enchanted link updates only. */
  private String providerId;
  /** Sent by enchanted link updates only. */
  private String templateId;
  /** Sent by enchanted link updates only. */
  private String locale;
  /** Sent by enchanted link updates only. */
  private Boolean failOnConflict;

  public UpdateOptions(boolean addToLoginIds, boolean onMergeUseExisting) {
    this(addToLoginIds, onMergeUseExisting, null, null, null, null);
  }
}