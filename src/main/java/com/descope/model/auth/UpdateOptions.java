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
  /** Supported by OTP updates only. Max length 254. */
  private String providerId;
  /** Supported by OTP updates only. Max length 254. */
  private String locale;
  /** Supported by OTP updates only. */
  private Boolean failOnConflict;
  /** Supported by OTP updates only: keeps the refresh token's auth methods and adds the updated one. */
  private Boolean mfa;

  public UpdateOptions(boolean addToLoginIds, boolean onMergeUseExisting) {
    this.addToLoginIds = addToLoginIds;
    this.onMergeUseExisting = onMergeUseExisting;
  }
}