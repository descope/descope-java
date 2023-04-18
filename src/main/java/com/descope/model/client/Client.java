package com.descope.model.client;

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
}
