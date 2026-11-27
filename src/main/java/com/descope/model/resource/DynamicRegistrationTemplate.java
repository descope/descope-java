package com.descope.model.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRegistrationTemplate {
  private String id;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long version;
  private String name;
  private String description;
  private String loginPageURL;
  private ThirdPartyApplicationSessionSettings sessionSettings;
  private Boolean skipConsentScreen;
  private Boolean forceAddAllAuthorizationInfo;
  private List<String> tags;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long createdTime;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long modifiedTime;
  private String fallbackClientLogo;
}
