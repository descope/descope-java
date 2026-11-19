package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.EMAIL_VERIFICATION_START_LINK;

import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.emailverification.EmailVerificationStartResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.EmailVerificationService;
import java.util.Collections;

class EmailVerificationServiceImpl extends AuthenticationServiceImpl implements EmailVerificationService {

  EmailVerificationServiceImpl(Client client) {
    super(client);
  }

  @Override
  public EmailVerificationStartResponse start() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(EMAIL_VERIFICATION_START_LINK), Collections.emptyMap(),
        EmailVerificationStartResponse.class);
  }
}
