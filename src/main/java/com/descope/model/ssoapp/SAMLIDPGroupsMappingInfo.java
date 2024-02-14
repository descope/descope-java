package com.descope.model.ssoapp;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SAMLIDPGroupsMappingInfo {
  private String name;
  private String type;
  private String filterType;
  private String value;
  private List<SAMLIDPRoleGroupMappingInfo> roles;
}
