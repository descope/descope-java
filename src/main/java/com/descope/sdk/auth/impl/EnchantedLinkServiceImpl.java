package com.descope.sdk.auth.impl;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.Masked;
import com.descope.model.magiclink.SignInRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.EnchantedLinkService;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

class EnchantedLinkServiceImpl extends AuthenticationServiceImpl
    implements EnchantedLinkService {


  EnchantedLinkServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public String signIn(DeliveryMethod deliveryMethod, String loginId, String uri, LoginOptions loginOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI enchantedLink = composeEnchantedLinkSignInURL(deliveryMethod);
    var signInRequest = new SignInRequest(uri, loginId, loginOptions);
    ApiProxy apiProxy = getApiProxy();
    var masked = apiProxy.post(enchantedLink, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUp(String loginId, String uri, User user) throws DescopeException {
    return null;
  }

  @Override
  public String signUpOrIn(DeliveryMethod deliveryMethod, String loginId, String uri) throws DescopeException {
    return null;
  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    return null;
  }

  @Override
  public AuthenticationInfo verify(String token) throws DescopeException {
    return null;
  }

  @Override
  public String updateUserEmail(String loginId, String email, String uri) throws DescopeException {
    return null;
  }

  private URI composeEnchantedLinkSignInURL(DeliveryMethod deliveryMethod) {
    return composeURI("", deliveryMethod.getValue());
  }
}
