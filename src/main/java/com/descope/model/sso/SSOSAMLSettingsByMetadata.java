package com.descope.model.sso;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOSAMLSettingsByMetadata {
  private String idpMetadataUrl;
  private AttributeMapping attributeMapping;
  private List<RoleMapping> roleMappings;
  private String spEncryptionKey;
  private String spSignKey;
  private String subjectNameIdFormat;
  /**
   * Audience values a SAML assertion may carry in place of the tenant's SP entity ID, for an IdP that sends a
   * different audience and can't be changed. Values match exactly, ignoring surrounding whitespace, and an empty
   * string accepts an empty audience. Up to 20 values of up to 1000 characters each.
   *
   * <p>{@code null} (the default) keeps the stored values. Any non-null list replaces them, so an empty list
   * clears them.
   */
  private List<String> acceptedAudiences;
  // NOTICE - the following fields should be overridden only in case of SSO migration,
  //          otherwise, do not modify these fields
  private String spACSUrl;
  private String spEntityId;

  public SSOSAMLSettingsByMetadata(String idpMetadataUrl, AttributeMapping attributeMapping,
      List<RoleMapping> roleMappings, String spEncryptionKey, String spSignKey, String subjectNameIdFormat,
      String spACSUrl, String spEntityId) {
    this(idpMetadataUrl, attributeMapping, roleMappings, spEncryptionKey, spSignKey, subjectNameIdFormat, null,
        spACSUrl, spEntityId);
  }

  // Tells the server that a non-null acceptedAudiences, empty included, replaces the stored values.
  @JsonProperty(value = "replaceAcceptedAudiences", access = JsonProperty.Access.READ_ONLY)
  private Boolean replaceAcceptedAudiences() {
    return acceptedAudiences != null ? Boolean.TRUE : null;
  }
}
