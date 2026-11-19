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
  // Fields below are sent by OTP updates only
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean failOnConflict;
  /** Keep the refresh token's auth methods and add the verified address to them; needs a valid refresh token. */
  private Boolean mfa;

  public UpdateOptions(boolean addToLoginIds, boolean onMergeUseExisting) {
    this.addToLoginIds = addToLoginIds;
    this.onMergeUseExisting = onMergeUseExisting;
  }
}