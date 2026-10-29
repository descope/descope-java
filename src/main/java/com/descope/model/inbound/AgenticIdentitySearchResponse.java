package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentitySearchResponse {
  private AgenticIdentity[] identities;
  private int total;
}
