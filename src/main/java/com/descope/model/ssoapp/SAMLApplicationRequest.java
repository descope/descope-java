package com.descope.model.ssoapp;

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
public class SAMLApplicationRequest {
  public static final String SIGNATURE_ALGORITHM_SHA256 = "sha256";
  public static final String SLO_BINDING_REDIRECT = "redirect";
  public static final String SLO_BINDING_POST = "post";
  public static final String SLO_BINDING_SOAP = "soap";
  public static final String SLO_PROPAGATION_NONE = "none";
  public static final String SLO_PROPAGATION_ALL_APPS = "allApps";
  public static final String SLO_SCOPE_SESSION = "session";
  public static final String SLO_SCOPE_ALL_SESSIONS = "allSessions";

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
  /**
   * Optional URL to redirect to on error.
   */
  private String errorRedirectUrl;
  /**
   * Optional signature algorithm for IdP-initiated flows: {@link #SIGNATURE_ALGORITHM_SHA256}, or empty for the
   * default SHA-1.
   */
  private String defaultSignatureAlgorithm;
  /**
   * Optional custom attribute values of the app, keyed by attribute name.
   */
  private Map<String, Object> customAttributes;
  /**
   * Optional sso application logo for dark mode.
   */
  private String darkLogo;
  /**
   * Optional project-level roles that grant access to this application, in addition to users assigned directly or
   * through their tenant. Tenant scoped roles are rejected.
   */
  private List<String> assignedRoleIds;
  /**
   * Optional SAML Single Logout (SLO). Off by default; an update without it turns it off.
   */
  private Boolean singleLogoutEnabled;
  /**
   * Optional SP SingleLogoutService URL. Required when single logout is on and the app is configured manually; empty
   * takes it from the SP metadata.
   */
  private String spSingleLogoutUrl;
  /**
   * Optional binding of spSingleLogoutUrl, one of the {@code SLO_BINDING_*} values.
   */
  private String spSingleLogoutBinding;
  /**
   * Optional reach of a logout the app's SP starts, one of the {@code SLO_PROPAGATION_*} values. Default: none.
   */
  private String singleLogoutPropagation;
  /**
   * Optional sessions a logout the app's SP starts ends, one of the {@code SLO_SCOPE_*} values. Default: session.
   */
  private String singleLogoutScope;
  /**
   * Optional: a Force Logout of the user sends the app a LogoutRequest. Needs single logout with the soap binding.
   */
  private Boolean notifyOnForceLogout;

  public SAMLApplicationRequest(String id, String name, String description, Boolean enabled, String logo,
      String loginPageUrl, Boolean useMetadataInfo, String metadataUrl, String entityId, String acsUrl,
      String certificate, List<SAMLIDPAttributeMappingInfo> attributeMapping,
      List<SAMLIDPGroupsMappingInfo> groupsMapping, List<String> acsAllowedCallbacks, String subjectNameIdType,
      String subjectNameIdFormat, String defaultRelayState, Boolean forceAuthentication, String logoutRedirectURL) {
    this(id, name, description, enabled, logo, loginPageUrl, useMetadataInfo, metadataUrl, entityId, acsUrl,
        certificate, attributeMapping, groupsMapping, acsAllowedCallbacks, subjectNameIdType, subjectNameIdFormat,
        defaultRelayState, forceAuthentication, logoutRedirectURL, null, null, null, null, null, null, null, null, null,
        null, null);
  }
}
