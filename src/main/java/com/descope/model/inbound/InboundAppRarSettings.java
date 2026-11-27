package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Rich Authorization Requests (RFC 9396) settings. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppRarSettings {
  private InboundAppRarTypeSchema[] schemas;
}
