package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.GENERATE_RECOVERY_CODES_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.recoverycodes.RecoveryCodesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.RecoveryCodesService;
import org.apache.commons.lang3.StringUtils;

class RecoveryCodesServiceImpl extends AuthenticationServiceImpl implements RecoveryCodesService {

  RecoveryCodesServiceImpl(Client client) {
    super(client);
  }

  @Override
  public RecoveryCodesResponse generate(String loginId, String refreshToken) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("loginId");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("refreshToken");
    }
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(getUri(GENERATE_RECOVERY_CODES_LINK), mapOf("loginId", loginId),
        RecoveryCodesResponse.class);
  }
}
