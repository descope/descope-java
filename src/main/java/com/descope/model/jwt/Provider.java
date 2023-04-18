package com.descope.model.jwt;

import com.descope.model.auth.AuthParams;
import com.descope.model.client.Client;
import java.security.Key;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Provider {
  private Client client;
  private AuthParams authParams;
  private Key providedKey;
  private Map<String, Key> keyMap;
}
