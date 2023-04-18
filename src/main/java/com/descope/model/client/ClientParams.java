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
public class ClientParams {
  private String baseUrl;
  private String projectId;
  private Map<String, String> customDefaultHeaders;
}
