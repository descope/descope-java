package com.descope.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundAppCreateResponse {
  private String id;
  private OutboundApp app;

  public OutboundAppCreateResponse(String id) {
    this.id = id;
  }
}
