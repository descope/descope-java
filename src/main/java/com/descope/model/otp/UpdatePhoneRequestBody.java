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
public class UpdatePhoneRequestBody {
  private String phone;
  private String loginId;
  @JsonProperty("addToLoginIDs")
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private Map<String, String> templateOptions;
}
