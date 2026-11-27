package com.descope.model.jwttemplate;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A read-only starter JWT template shipped by Descope. String values match the {@link JwtTemplate} constants. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTemplateLibraryEntry {
  private String id;
  private String name;
  private String description;
  private Map<String, Object> template;
  private List<String> tags;
  private String authSchema;
  private String type;
  private Boolean conformanceIssuer;
  private Boolean autoDCT;
  private Boolean enforceIssuer;
  private String emptyClaimPolicy;
  private Boolean overrideSubject;
  private String issuerType;
  private Boolean omitCustomClaimsFromDSR;
  private Boolean addJti;
  private Boolean excludePermissions;
  private Boolean experimental;
  private String logoLight;
  private String logoDark;
}
