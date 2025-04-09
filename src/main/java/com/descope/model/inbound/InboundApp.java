package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundApp {
  private String id;
  private String name;
  private String description; 
  private String logo;
  private String loginPageUrl;
  private String clientId;
  private String[] approvedCallbackUrls;
  private InboundAppScope[] permissionsScopes;
  private InboundAppScope[] attributesScopes;
}
