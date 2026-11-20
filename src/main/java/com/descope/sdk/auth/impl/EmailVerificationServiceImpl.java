package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.EMAIL_VERIFICATION_START_LINK;

import com.descope.exception.DescopeException;
import com.descope.model.auth.EmailVerificationStartResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.EmailVerificationService;
import java.util.Collections;
import java.util.Map;

class EmailVerificationServiceImpl extends AuthenticationServiceImpl implements EmailVerificationService {

  EmailVerificationServiceImpl(Client client) {
    super(client);
  }

  @Override
  public String start() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = Collections.emptyMap();
    return apiProxy.post(getUri(EMAIL_VERIFICATION_START_LINK), request, EmailVerificationStartResponse.class)
        .getNonce();
  }
}
