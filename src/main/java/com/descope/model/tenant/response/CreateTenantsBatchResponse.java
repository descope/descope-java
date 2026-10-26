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
public class CreateTenantsBatchResponse {
  List<CreatedTenantResponse> createdTenants;
  List<CreateTenantBatchFailureResponse> failedTenants;
  Map<String, String> additionalErrors;
}
