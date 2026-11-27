package com.descope.model.notp;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class NOTPUpdateUserRequest {
  private String loginId;
  private String phone;
  @JsonProperty("addToLoginIDs")
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private Map<String, String> templateOptions;
}
