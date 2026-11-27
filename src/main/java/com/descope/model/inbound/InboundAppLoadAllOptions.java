package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppLoadAllOptions {
  /** Page number, starting with 0. */
  private Integer page;
  /** 0 returns the default amount. */
  private Integer limit;
  /** Returns only apps whose owner display name matches, ignoring case. */
  private String createdBy;
  /** Returns only Agentic Identity Hub clients (true) or only apps that are not (false). */
  private Boolean agentic;
}
