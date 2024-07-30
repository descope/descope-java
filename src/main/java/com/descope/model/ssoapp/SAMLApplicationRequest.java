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
public class SAMLApplicationRequest {
  /**
   * Optional sso application ID. Will be auto-generated if not given. Must be unique per project.
   */
  private String id;
  /**
   * The sso application's name. Must be unique per project.
   */
  private String name;
  /**
   * Optional sso application description.
   */
  private String description;
  /**
   * Optional set the sso application as enabled or disabled.
   */
  private Boolean enabled;
  /**
   * Optional sso application logo.
   */
  private String logo;
  /**
   * The URL where login page is hosted.
   */
  private String loginPageUrl;
  /**
   * Optional determine if SP info should be automatically fetched from metadata_url
   * or by specified it by the entity_id, acs_url, certificate parameters.
   */
  private Boolean useMetadataInfo;
  /**
   * Optional (must be set if UseMetadataInfo is True) SP metadata url which include all the SP SAML info.
   */
  private String metadataUrl;
  /**
   * Optional (must be set if UseMetadataInfo is False) SP entity id.
   */
  private String entityId;
  /**
   * Optional (must be set if UseMetadataInfo is False) SP ACS (saml callback) url.
   */
  private String acsUrl;
  /**
   * Optional (must be set if UseMetadataInfo is False)SP certificate, relevant only when SAML request must be signed.
   */
  private String certificate;
  /**
   * Optional list of Descope (IdP) attributes to SP mapping.
   */
  private List<SAMLIDPAttributeMappingInfo> attributeMapping;
  /**
   * Optional list of Descope (IdP) roles that will be mapped to SP groups.
   */
  private List<SAMLIDPGroupsMappingInfo> groupsMapping;
  /**
   * Optional list of urls wildcards strings represents the allowed ACS urls that will be accepted while arriving on the
   * SAML request as SP callback urls.
   */
  private List<String> acsAllowedCallbacks;
  /**
   * Optional define the SAML Assertion subject name type, leave empty for using Descope user-id or set to
   * "email"/"phone".
   */
  private String subjectNameIdType;
  /**
   * Optional define the SAML Assertion subject name format, leave empty for using
   * "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified".
   */
  private String subjectNameIdFormat;
  /**
   * Optional default relay state value that will be used in the SAML request.
   */
  private String defaultRelayState;
  /**
   * Optional determine if the IdP should force the user to re-authenticate.
   */
  private Boolean forceAuthentication;
  /**
   * Optional Target URL to which the user will be redirected upon logout completion.
   */
  private String logoutRedirectURL;
}
