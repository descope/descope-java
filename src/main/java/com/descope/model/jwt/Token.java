package com.descope.model.jwt;

import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
  private Date refreshExpiration;
  private Long expiration;
  private String jwt;
  private String id;
  private String projectId;
  private Map<String, Object> claims;
}
