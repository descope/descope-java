package com.descope.model.user.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserHistoryResponse {
  String userId;
  Integer loginTime;
  String city;
  String country;
  String ip;

  public Instant getLoginTimeInstant() {
    return Instant.ofEpochSecond(loginTime);
  }
}
