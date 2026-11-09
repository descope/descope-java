package com.descope.model.magiclink.request;

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
public class UpdatePhoneRequest {
  private String phone;
  private String loginId;
  @JsonProperty("URI")
  private String uri;
  private boolean crossDevice;
  @JsonProperty("addToLoginIDs")
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private Map<String, String> templateOptions;
  private String providerId;
  private String locale;
  private Boolean failOnConflict;

  public UpdatePhoneRequest(String phone, String loginId, String uri, boolean crossDevice, boolean addToLoginIds,
      boolean onMergeUseExisting, Map<String, String> templateOptions) {
    this(phone, loginId, uri, crossDevice, addToLoginIds, onMergeUseExisting, templateOptions, null, null, null);
  }
}
