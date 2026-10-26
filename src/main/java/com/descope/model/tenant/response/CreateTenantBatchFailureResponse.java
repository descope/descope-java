package com.descope.model.tenant.response;

import com.descope.model.tenant.request.CreateTenantRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantBatchFailureResponse {
  String failure;
  CreateTenantRequest tenant;
}
