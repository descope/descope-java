package com.descope.model.tenant.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantsBatchResponse {
  private List<CreatedTenantResponse> createdTenants;
  private List<CreateTenantBatchFailureResponse> failedTenants;
  private Map<String, String> additionalErrors;
}
