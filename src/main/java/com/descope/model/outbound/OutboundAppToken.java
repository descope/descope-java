package com.descope.model.outbound;

import com.descope.utils.InstantToMillisSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundAppToken {
  private String id;
  private String appId;
  private String userId;
  private String tokenSub;
  private String accessToken;
  private String accessTokenType;
  @JsonSerialize(using = InstantToMillisSerializer.class)
  private Instant accessTokenExpiry;
  private boolean hasRefreshToken;
  private String refreshToken;
  @JsonSerialize(using = InstantToMillisSerializer.class)
  private Instant lastRefreshTime;
  private String lastRefreshError;
  private List<String> scopes;
}