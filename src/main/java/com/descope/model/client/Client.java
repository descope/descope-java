package com.descope.model.client;

import com.descope.model.jwt.Provider;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
  private String uri;
  private Map<String, String> headers;
  private ClientParams params;
  private SdkInfo sdkInfo;
  @Builder.Default
  private Provider provider = Provider.builder().keyMap(new HashMap<>()).build();

  public synchronized Key getProvidedKey() {
    return provider.getProvidedKey();
  }

  public synchronized void setProvidedKey(Key key) {
    provider.setProvidedKey(key);
  }
}
