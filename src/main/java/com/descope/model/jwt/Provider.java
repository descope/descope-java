package com.descope.model.jwt;

import com.descope.model.client.Client;
import java.security.Key;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Provider {
  private Client client;
  private String projectId;
  private Key providedKey; // Probably provided by Client
  private Map<String, Key> keyMap; // If no key was provided | key is Project ID
}
