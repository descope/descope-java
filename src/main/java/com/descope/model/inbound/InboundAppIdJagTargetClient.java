package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A target client for Cross-App Access (ID-JAG). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppIdJagTargetClient {
  private String targetClientId;
}
