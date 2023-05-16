package com.descope.model.tenant.response;

import com.descope.model.tenant.Tenant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllTenantsResponse {
  List<Tenant> tenants;
}
