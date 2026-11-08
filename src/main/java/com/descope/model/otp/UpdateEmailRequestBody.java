package com.descope.model.otp;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmailRequestBody {
  private String email;
  private String loginId;
  @JsonProperty("addToLoginIDs")
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private Map<String, String> templateOptions;
  private String providerId;
  private String locale;
  private Boolean failOnConflict;
  private Boolean mfa;

  public UpdateEmailRequestBody(String email, String loginId, boolean addToLoginIds, boolean onMergeUseExisting,
      Map<String, String> templateOptions) {
    this.email = email;
    this.loginId = loginId;
    this.addToLoginIds = addToLoginIds;
    this.onMergeUseExisting = onMergeUseExisting;
    this.templateOptions = templateOptions;
  }
}
