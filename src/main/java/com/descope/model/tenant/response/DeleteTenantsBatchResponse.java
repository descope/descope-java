package com.descope.model.tenant.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTenantsBatchResponse {
  List<String> deletedTenantIds;
  List<DeleteTenantBatchFailureResponse> failedTenants;
  Map<String, String> additionalErrors;
}
