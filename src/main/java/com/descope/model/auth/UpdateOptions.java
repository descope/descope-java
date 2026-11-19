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
  // Sent by the magic link update methods only
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean failOnConflict;

  public UpdateOptions(boolean addToLoginIds, boolean onMergeUseExisting) {
    this(addToLoginIds, onMergeUseExisting, null, null, null, null);
  }
}