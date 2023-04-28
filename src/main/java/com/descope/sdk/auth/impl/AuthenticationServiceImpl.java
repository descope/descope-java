package com.descope.sdk.auth.impl;

import com.descope.model.auth.AuthParams;
import com.descope.model.client.Client;
import com.descope.sdk.auth.AuthenticationService;

class AuthenticationServiceImpl extends AuthenticationsBase implements AuthenticationService {
  
  AuthenticationServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }
}
