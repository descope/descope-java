package com.descope.model.magement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagementParams {
  private String projectId;
  private String managementKey;
}
