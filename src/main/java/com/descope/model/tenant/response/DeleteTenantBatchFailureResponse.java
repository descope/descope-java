package com.descope.model.tenant.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTenantBatchFailureResponse {
  String failure;
  String id;
}
