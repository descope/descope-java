package com.descope.model.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SdkInfo {
  private String name;
  private String version;
  private String javaVersion;
  private String sha;
}
