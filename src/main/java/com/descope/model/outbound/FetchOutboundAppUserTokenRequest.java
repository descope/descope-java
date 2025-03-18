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
public class FetchOutboundAppUserTokenRequest {
  private String appId;
  private String userId;
  private List<String> scopes;
  private FetchOutboundAppTokenOptions options;
}
