package com.descope.model.jwttemplate;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A JWT template. Fields left {@code null} are not sent. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTemplate {
  public static final String TYPE_USER = "user";
  public static final String TYPE_KEY = "key";

  public static final String AUTH_SCHEMA_DEFAULT = "default";
  public static final String AUTH_SCHEMA_TENANT_ONLY = "tenantOnly";
  public static final String AUTH_SCHEMA_NONE = "none";

  public static final String EMPTY_CLAIM_POLICY_NONE = "none";
  public static final String EMPTY_CLAIM_POLICY_NIL = "nil";
  public static final String EMPTY_CLAIM_POLICY_DELETE = "delete";

  public static final String ISSUER_TYPE_LEGACY = "legacy";
  public static final String ISSUER_TYPE_INBOUND = "inbound";
  public static final String ISSUER_TYPE_FEDERATED = "federated";

  private String id;
  /** Required on create and update. */
  private String name;
  private String description;
  private Map<String, Object> template;
  private String source;
  private List<String> tags;
  /** One of the {@code AUTH_SCHEMA_*} values. */
  private String authSchema;
  /** Required on create and update. One of the {@code TYPE_*} values. */
  private String type;
  private Boolean conformanceIssuer;
  private Boolean autoDCT;
  private Boolean enforceIssuer;
  /** One of the {@code EMPTY_CLAIM_POLICY_*} values. */
  private String emptyClaimPolicy;
  private Boolean overrideSubject;
  /** One of the {@code ISSUER_TYPE_*} values. */
  private String issuerType;
  private Boolean omitCustomClaimsFromDSR;
  private Boolean addJti;
  private Boolean excludePermissions;
}
