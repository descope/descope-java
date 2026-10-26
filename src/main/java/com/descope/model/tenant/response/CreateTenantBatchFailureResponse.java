package com.descope.model.tenant.response;

import com.descope.model.tenant.request.CreateTenantRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantBatchFailureResponse {
  String failure;
  CreateTenantRequest tenant;
}
