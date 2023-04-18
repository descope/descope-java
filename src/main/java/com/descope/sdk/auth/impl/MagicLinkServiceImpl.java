package com.descope.sdk.auth.impl;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_MAGIC_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_MAGIC_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_MAGIC_LINK;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.JWTResponse;
import com.descope.model.jwt.Token;
import com.descope.model.magiclink.Masked;
import com.descope.model.magiclink.SignInRequest;
import com.descope.model.magiclink.SignUpRequest;
import com.descope.model.magiclink.VerifyRequest;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.MagicLinkService;
import java.net.URI;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

class MagicLinkServiceImpl extends AuthenticationsBase
    implements MagicLinkService, AuthenticationService {

  MagicLinkServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public String signIn(DeliveryMethod deliveryMethod, String loginId, String uri)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI magicLinkSignInURL = composeMagicLinkSignInURI(deliveryMethod);
    var signInRequest = new SignInRequest(uri, loginId);

    var apiProxy = getApiProxy();
    var masked = apiProxy.post(magicLinkSignInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user)
      throws DescopeException {
    if (Objects.isNull(user)) {
      user = new User();
    }

    verifyDeliveryMethod(deliveryMethod, loginId, user);
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI magicLinkSignUpURL = composeMagicLinkSignUpURI(deliveryMethod);

    var signUpRequestBuilder = SignUpRequest.builder().loginId(loginId).uri(uri);
    if (EMAIL.equals(deliveryMethod)) {
      signUpRequestBuilder.email(user.getEmail());
    }

    var signUpRequest = signUpRequestBuilder.user(user).build();
    var apiProxy = getApiProxy();
    var masked = apiProxy.post(magicLinkSignUpURL, signUpRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public AuthenticationInfo verify(String token) {
    URI verifyMagicLinkURL = composeVerifyMagicLinkURL();
    var verifyRequest = new VerifyRequest(token);
    var apiProxy = getApiProxy();
    var jwtResponse = apiProxy.post(verifyMagicLinkURL, verifyRequest, JWTResponse.class);

    Token sessionToken = createToken(jwtResponse.getSessionJwt());
    Token refreshToken = createToken(jwtResponse.getRefreshJwt());

    // TODO - Set Cookies | 18/04/23 | by keshavram

    return new AuthenticationInfo(
        sessionToken, refreshToken, jwtResponse.getUser(), jwtResponse.getFirstSeen());
  }

  private URI composeMagicLinkSignInURI(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_IN_MAGIC_LINK, deliveryMethod.getValue());
  }

  private URI composeMagicLinkSignUpURI(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_UP_MAGIC_LINK, deliveryMethod.getValue());
  }

  private URI composeVerifyMagicLinkURL() {
    return getUri(VERIFY_MAGIC_LINK);
  }
}
