package com.descope.model.tenant.response;

import com.descope.model.tenant.request.UpdateTenantRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantBatchFailureResponse {
  String failure;
  UpdateTenantRequest tenant;
}
