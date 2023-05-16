package com.descope.model.tenant;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
  String id;
  String name;
  List<String> selfProvisioningDomains;
}
