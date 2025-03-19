package com.descope.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchOutboundAppTokenOptions {
  private Boolean withRefreshToken;
  private Boolean forceRefresh;
}
