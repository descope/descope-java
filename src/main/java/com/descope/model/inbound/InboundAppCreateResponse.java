package com.descope.model.inbound;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppCreateResponse {
  private String id;
  @JsonProperty("cleartext")
  private String secret;
  private String clientId;

  public InboundAppCreateResponse(String id, String secret) {
    this.id = id;
    this.secret = secret;
  }
}

