package com.descope.sdk.auth.impl;

import com.descope.model.auth.AuthParams;
import com.descope.model.client.Client;
import com.descope.sdk.auth.AuthenticationService;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthenticationServiceBuilder {
  public static AuthenticationService buildService(Client client, AuthParams authParams) {
    return new MagicLinkServiceImpl(client, authParams);
  }
}
