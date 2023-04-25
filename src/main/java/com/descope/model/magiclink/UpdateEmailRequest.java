package com.descope.model.magiclink;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmailRequest {

  private String email;
  private String loginId;
  @JsonProperty("URI")
  private String uri;
  private boolean crossDevice;
}
