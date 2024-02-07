package com.descope.model.tenant;

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
public class Tenant {
  String id;
  String name;
  List<String> selfProvisioningDomains;
  Map<String, Object> customAttributes;
  String authType;
  List<String> domains;
}
