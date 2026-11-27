package com.descope.model.tenant.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTenantsBatchResponse {
  private List<String> deletedTenantIds;
  private List<DeleteTenantBatchFailureResponse> failedTenants;
  private Map<String, String> additionalErrors;
}
