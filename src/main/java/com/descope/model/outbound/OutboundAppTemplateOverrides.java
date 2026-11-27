package com.descope.model.outbound;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundAppTemplateOverrides {
  private String name;
  private String description;
  private String logo;
  private String discoveryUrl;
  private String authorizationUrl;
  private List<OutboundAppURLParam> authorizationUrlParams;
  private String tokenUrl;
  private List<OutboundAppURLParam> tokenUrlParams;
  private String revocationUrl;
  private List<String> defaultScopes;
  private String defaultRedirectUrl;
  private String callbackDomain;
  private Boolean pkce;
  // See OutboundAppConstants.ACCESS_TYPE_*
  private String accessType;
  // See OutboundAppConstants.PROMPT_*
  private List<String> prompt;
  private Boolean useDcr;
  private String dcrUrl;
}
