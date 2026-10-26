package com.descope.model.tenant.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantsBatchResponse {
  List<String> updatedTenantIds;
  List<UpdateTenantBatchFailureResponse> failedTenants;
  Map<String, String> additionalErrors;
}
