package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.COMPOSE_OTP_FOR_TEST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.CREATE_USERS_BATCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.CREATE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_ALL_TEST_USERS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.ENCHANTED_LINK_FOR_TEST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.GET_PROVIDER_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOGOUT_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MAGIC_LINK_FOR_TEST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_CUSTOM_ATTRIBUTE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_PICTURE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_USER_LOGIN_ID_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_USER_NAME_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_ADD_ROLES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_ADD_SSO_APPS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_ADD_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_CREATE_EMBEDDED_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_EXPIRE_PASSWORD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_HISTORY_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_REMOVE_ROLES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_REMOVE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SEARCH_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SET_PASSWORD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SET_TEMPORARY_PASSWORD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SET_ACTIVE_PASSWORD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SET_ROLES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SET_SSO_APPS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_UPDATE_EMAIL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_UPDATE_PHONE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_UPDATE_STATUS_LINK;
import static com.descope.utils.CollectionUtils.addIfNotBlank;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.InviteOptions;
import com.descope.model.client.Client;
import com.descope.model.user.request.BatchUserRequest;
import com.descope.model.user.request.EnchantedLinkTestUserRequest;
import com.descope.model.user.request.GenerateEmbeddedLinkRequest;
import com.descope.model.user.request.MagicLinkTestUserRequest;
import com.descope.model.user.request.OTPTestUserRequest;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.AllUsersResponseDetails;
import com.descope.model.user.response.EnchantedLinkTestUserResponse;
import com.descope.model.user.response.GenerateEmbeddedLinkResponse;
import com.descope.model.user.response.MagicLinkTestUserResponse;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.model.user.response.ProviderTokenResponse;
import com.descope.model.user.response.UserHistoryResponse;
import com.descope.model.user.response.UserResponseDetails;
import com.descope.model.user.response.UsersBatchResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

class UserServiceImpl extends ManagementsBase implements UserService {

  UserServiceImpl(Client client) {
    super(client);
  }

  @Override
  public UserResponseDetails create(String loginId, UserRequest request) throws DescopeException {
    if (request == null) {
      request = new UserRequest();
    }
    Map<String, Object> req = mapOf("loginId", loginId);
    request.setTest(false);
    req.putAll(request.toMap());
    URI createUserUri = composeCreateUserUri();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, req, UserResponseDetails.class);
  }

  @Override
  public UsersBatchResponse createBatch(List<BatchUserRequest> users) throws DescopeException {
    if (users == null || users.isEmpty()) {
      throw ServerCommonException.invalidArgument("Users");
    }
    for (BatchUserRequest u : users) {
      u.setTest(false);
    }
    URI createUsersUri = composeCreateBatchUsersUri();
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> req = mapOf("users", users, "invite", false);
    return apiProxy.post(createUsersUri, req, UsersBatchResponse.class);
  }

  @Override
  public UserResponseDetails createTestUser(String loginId, UserRequest request)
      throws DescopeException {
    if (request == null) {
      request = new UserRequest();
    }
    Map<String, Object> req = mapOf("loginId", loginId, "invite", false);
    request.setTest(true);
    req.putAll(request.toMap());
    URI createUserUri = composeCreateUserUri();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, req, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails invite(String loginId, UserRequest request, InviteOptions options)
      throws DescopeException {
    if (request == null) {
      request = new UserRequest();
    }
    Map<String, Object> req = mapOf("loginId", loginId, "invite", true);
    request.setTest(false);
    if (options != null) {
      addIfNotBlank(req, "inviteUrl", options.getInviteUrl());
      addIfNotNull(req, "sendSMS", options.getSendSMS());
      addIfNotNull(req, "sendEmail", options.getSendEmail());
    }
    URI createUserUri = composeCreateUserUri();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, req, UserResponseDetails.class);
  }

  @Override
  public UsersBatchResponse inviteBatch(List<BatchUserRequest> users, InviteOptions options) throws DescopeException {
    if (users == null || users.isEmpty()) {
      throw ServerCommonException.invalidArgument("Users");
    }
    for (BatchUserRequest u : users) {
      u.setTest(false);
    }
    Map<String, Object> req = mapOf("users", users, "invite", true);
    if (options != null) {
      addIfNotBlank(req, "inviteUrl", options.getInviteUrl());
      addIfNotNull(req, "sendSMS", options.getSendSMS());
      addIfNotNull(req, "sendEmail", options.getSendEmail());
    }
    URI createUsersUri = composeCreateBatchUsersUri();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(createUsersUri, req, UsersBatchResponse.class);
  }

  @Override
  public UserResponseDetails update(String loginId, UserRequest request) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (request == null) {
      request = new UserRequest();
    }
    Map<String, Object> req = mapOf("loginId", loginId);
    req.putAll(request.toMap());
    URI updateUserUri = composeUpdateUserUri();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updateUserUri, req, UserResponseDetails.class);
  }

  @Override
  public void delete(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI deleteUserUri = composeDeleteUserUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(deleteUserUri, mapOf("loginId", loginId), Void.class);
  }

  @Override
  public void logoutUser(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI logoutUserUri = composeLogoutUserUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(logoutUserUri, mapOf("loginId", loginId), Void.class);
  }

  @Override
  public void logoutUserByUserId(String userId) throws DescopeException {
    if (StringUtils.isBlank(userId)) {
      throw ServerCommonException.invalidArgument("User ID");
    }
    URI logoutUserUri = composeLogoutUserUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(logoutUserUri, mapOf("userId", userId), Void.class);
  }

  @Override
  public void deleteAllTestUsers() throws DescopeException {
    URI deleteAllTestUsersUri = composeDeleteAllTestUsersUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.delete(deleteAllTestUsersUri, null, Void.class);
  }

  @Override
  public UserResponseDetails load(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI loadUserUri = composeLoadUserUri(mapOf("loginId", loginId));
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(loadUserUri, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails loadByUserId(String userId) throws DescopeException {
    if (StringUtils.isBlank(userId)) {
      throw ServerCommonException.invalidArgument("User ID");
    }
    URI loadUserUri = composeLoadUserUri(mapOf("userId", userId));
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(loadUserUri, UserResponseDetails.class);
  }

  @Override
  public AllUsersResponseDetails searchAll(UserSearchRequest request)
      throws DescopeException {
    if (request == null) {
      request = UserSearchRequest.builder().limit(0).page(0).build();
    }
    if (request.getLimit() == null || request.getLimit() < 0) {
      throw ServerCommonException.invalidArgument("limit");
    }
    if (request.getPage() == null || request.getPage() < 0) {
      throw ServerCommonException.invalidArgument("page");
    }

    URI composeSearchAllUri = composeSearchAllUri();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(composeSearchAllUri, request, AllUsersResponseDetails.class);
  }

  @Override
  public UserResponseDetails activate(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI activateUserUri = composeActivateUserUri();
    Map<String, String> request = mapOf("loginId", loginId, "status", "enabled");
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(activateUserUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails deactivate(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI activateUserUri = composeActivateUserUri();
    Map<String, String> request = mapOf("loginId", loginId, "status", "disabled");
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(activateUserUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateEmail(String loginId, String email, Boolean isVerified)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updateEmailUri = composeUpdateEmailUri();
    Map<String, Object> request = mapOf("loginId", loginId, "email", email, "verified", isVerified);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updateEmailUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updatePhone(String loginId, String phone, Boolean isVerified)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updatePhoneUri = composeUpdatePhoneUri();
    Map<String, Object> request = mapOf("loginId", loginId, "phone", phone, "verified", isVerified);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updatePhoneUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateDisplayName(String loginId, String displayName)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updateUserNameUri = composeUpdateUserNameUri();
    Map<String, Object> request = mapOf("loginId", loginId, "displayName", displayName);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updateUserNameUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateDisplayNames(String loginId, String givenName, String middleName,
      String familyName) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updateUserNameUri = composeUpdateUserNameUri();
    Map<String, Object> request = mapOf("loginId", loginId, "givenName", givenName, "middleName", middleName,
        "familyName", familyName);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updateUserNameUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updatePicture(String loginId, String picture) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updatePictureUri = composeUpdatePictureUri();
    Map<String, Object> request = mapOf("loginId", loginId, "picture", picture);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updatePictureUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateCustomAttributes(String loginId, String key, Object value)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(key)) {
      throw ServerCommonException.invalidArgument("Key");
    }
    URI updateAttributesUri = composeUpdateAttributesUri();
    Map<String, Object> request = mapOf("loginId", loginId, "attributeKey", key, "attributeValue", value);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updateAttributesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateLoginId(String loginId, String newLoginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updateLoginIdUri = composeUpdateLoginIdUri();
    Map<String, Object> request = mapOf("loginId", loginId, "newLoginId", newLoginId);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(updateLoginIdUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails setRoles(String loginId, List<String> roles) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addRolesUri = composeSetRolesUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", "", "roleNames", roles);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(addRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails addRoles(String loginId, List<String> roles) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addRolesUri = composeAddRolesUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", "", "roleNames", roles);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(addRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails removeRoles(String loginId, List<String> roles)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI removeRolesUri = composeRemoveRolesUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", "", "roleNames", roles);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(removeRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails addSsoApps(String loginId, List<String> ssoAppIds) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    Map<String, Object> request = mapOf("loginId", loginId, "ssoAppIds", ssoAppIds);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(USER_ADD_SSO_APPS_LINK), request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails setSsoApps(String loginId, List<String> ssoAppIds) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    Map<String, Object> request = mapOf("loginId", loginId, "ssoAppIds", ssoAppIds);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(USER_SET_SSO_APPS_LINK), request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails removeSsoApps(String loginId, List<String> ssoAppIds) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    Map<String, Object> request = mapOf("loginId", loginId, "ssoAppIds", ssoAppIds);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(USER_SET_SSO_APPS_LINK), request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails addTenant(String loginId, String tenantId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addTenantUri = composeAddTenantUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", tenantId);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(addTenantUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails removeTenant(String loginId, String tenantId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI removeTenantUri = composeRemoveTenantUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", tenantId);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(removeTenantUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails setTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addTenantRolesUri = composeSetTenantRolesUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", tenantId, "roleNames", roles);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(addTenantRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails addTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addTenantRolesUri = composeAddTenantRolesUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", tenantId, "roleNames", roles);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(addTenantRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails removeTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI removeTenantRolesUri = composeRemoveTenantRolesUri();
    Map<String, Object> request = mapOf("loginId", loginId, "tenantId", "", "roleNames", roles);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(removeTenantRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public void setTemporaryPassword(String loginId, String password) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(password)) {
      throw ServerCommonException.invalidArgument("Password");
    }
    URI setPasswordUri = composeSetTemporaryPasswordUri();
    Map<String, Object> request = mapOf("loginId", loginId, "password", password, "setActive", false);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(setPasswordUri, request, Void.class);
  }

  @Override
  public void setActivePassword(String loginId, String password) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(password)) {
      throw ServerCommonException.invalidArgument("Password");
    }
    URI setPasswordUri = composeSetActivePasswordUri();
    Map<String, Object> request = mapOf("loginId", loginId, "password", password, "setActive", true);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(setPasswordUri, request, Void.class);
  }

 /* Deprecated */
  @Override
  public void setPassword(String loginId, String password) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(password)) {
      throw ServerCommonException.invalidArgument("Password");
    }
    URI setPasswordUri = composeSetPasswordUri();
    Map<String, Object> request = mapOf("loginId", loginId, "password", password);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(setPasswordUri, request, Void.class);
  }

  @Override
  public void expirePassword(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI expirePasswordUri = composeExpirePasswordUri();
    Map<String, Object> request = mapOf("loginId", loginId);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(expirePasswordUri, request, Void.class);
  }

  @Override
  public ProviderTokenResponse getProviderToken(String loginId, String provider)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(provider)) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    URI getProviderTokenUri = composeGetProviderTokenUri(mapOf("loginId", loginId, "provider", provider));
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getProviderTokenUri, ProviderTokenResponse.class);
  }

  @Override
  public OTPTestUserResponse generateOtpForTestUser(String loginId, DeliveryMethod deliveryMethod)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI otpForTestUserUri = composeOTPForTestUserUri();
    OTPTestUserRequest request = new OTPTestUserRequest(loginId, deliveryMethod.getValue());
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(otpForTestUserUri, request, OTPTestUserResponse.class);
  }

  @Override
  public MagicLinkTestUserResponse generateMagicLinkForTestUser(
      String loginId, String uri, DeliveryMethod deliveryMethod)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI maginLinkForTestUserUri = composeMaginLinkForTestUserUri();
    MagicLinkTestUserRequest request = new MagicLinkTestUserRequest(loginId, deliveryMethod.getValue(), uri);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(maginLinkForTestUserUri, request, MagicLinkTestUserResponse.class);
  }

  @Override
  public EnchantedLinkTestUserResponse generateEnchantedLinkForTestUser(
      String loginId, String uri) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI enchantedLinkForTestUserUri = composeEnchantedLinkForTestUserUri();
    EnchantedLinkTestUserRequest request = new EnchantedLinkTestUserRequest(loginId, uri);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(enchantedLinkForTestUserUri, request, EnchantedLinkTestUserResponse.class);
  }

  @Override
  public List<UserHistoryResponse> history(List<String> userIds) throws DescopeException {
    if (CollectionUtils.isEmpty(userIds)) {
      throw ServerCommonException.invalidArgument("User IDs");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.postAndGetArray(getUri(USER_HISTORY_LINK), userIds,
        new TypeReference<List<UserHistoryResponse>>() {});
  }

  public String generateEmbeddedLink(
      String loginId, Map<String, Object> customClaims) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI generateEmbeddedLinkUri = composeGenerateEmbeddedLink();
    GenerateEmbeddedLinkRequest request = new GenerateEmbeddedLinkRequest(loginId, customClaims);
    ApiProxy apiProxy = getApiProxy();
    GenerateEmbeddedLinkResponse response =
        apiProxy.post(generateEmbeddedLinkUri, request, GenerateEmbeddedLinkResponse.class);
    return response.getToken();
  }

  private URI composeCreateUserUri() {
    return getUri(CREATE_USER_LINK);
  }

  private URI composeCreateBatchUsersUri() {
    return getUri(CREATE_USERS_BATCH_LINK);
  }

  private URI composeUpdateUserUri() {
    return getUri(UPDATE_USER_LINK);
  }

  private URI composeDeleteUserUri() {
    return getUri(DELETE_USER_LINK);
  }

  private URI composeLogoutUserUri() {
    return getUri(LOGOUT_USER_LINK);
  }

  private URI composeDeleteAllTestUsersUri() {
    return getUri(DELETE_ALL_TEST_USERS_LINK);
  }

  private URI composeLoadUserUri(Map<String, String> params) {
    return getQueryParamUri(LOAD_USER_LINK, params);
  }

  private URI composeSearchAllUri() {
    return getUri(USER_SEARCH_ALL_LINK);
  }

  private URI composeActivateUserUri() {
    return getUri(USER_UPDATE_STATUS_LINK);
  }

  private URI composeUpdateEmailUri() {
    return getUri(USER_UPDATE_EMAIL_LINK);
  }

  private URI composeUpdatePhoneUri() {
    return getUri(USER_UPDATE_PHONE_LINK);
  }

  private URI composeUpdateUserNameUri() {
    return getUri(UPDATE_USER_NAME_LINK);
  }

  private URI composeUpdateAttributesUri() {
    return getUri(UPDATE_CUSTOM_ATTRIBUTE_LINK);
  }

  private URI composeUpdatePictureUri() {
    return getUri(UPDATE_PICTURE_LINK);
  }

  private URI composeUpdateLoginIdUri() {
    return getUri(UPDATE_USER_LOGIN_ID_LINK);
  }

  private URI composeSetRolesUri() {
    return getUri(USER_SET_ROLES_LINK);
  }

  private URI composeAddRolesUri() {
    return getUri(USER_ADD_ROLES_LINK);
  }

  private URI composeRemoveRolesUri() {
    return getUri(USER_REMOVE_ROLES_LINK);
  }

  private URI composeAddTenantUri() {
    return getUri(USER_ADD_TENANT_LINK);
  }

  private URI composeRemoveTenantUri() {
    return getUri(USER_REMOVE_TENANT_LINK);
  }

  private URI composeSetTenantRolesUri() {
    return getUri(USER_SET_ROLES_LINK);
  }

  private URI composeAddTenantRolesUri() {
    return getUri(USER_ADD_ROLES_LINK);
  }

  private URI composeRemoveTenantRolesUri() {
    return getUri(USER_REMOVE_TENANT_LINK);
  }

  private URI composeGetProviderTokenUri(Map<String, String> params) {
    return getQueryParamUri(GET_PROVIDER_TOKEN, params);
  }

  private URI composeOTPForTestUserUri() {
    return getUri(COMPOSE_OTP_FOR_TEST_LINK);
  }

  private URI composeMaginLinkForTestUserUri() {
    return getUri(MAGIC_LINK_FOR_TEST_LINK);
  }

  private URI composeEnchantedLinkForTestUserUri() {
    return getUri(ENCHANTED_LINK_FOR_TEST_LINK);
  }

  private URI composeSetPasswordUri() {
    return getUri(USER_SET_PASSWORD_LINK);
  }

   private URI composeSetTemporaryPasswordUri() {
    return getUri(USER_SET_PASSWORD_LINK);
  }

   private URI composeSetActivePasswordUri() {
    return getUri(USER_SET_PASSWORD_LINK);
  }

  private URI composeExpirePasswordUri() {
    return getUri(USER_EXPIRE_PASSWORD_LINK);
  }

  private URI composeGenerateEmbeddedLink() {
    return getUri(USER_CREATE_EMBEDDED_LINK);
  }
}
