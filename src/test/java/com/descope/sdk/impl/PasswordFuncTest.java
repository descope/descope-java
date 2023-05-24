package com.descope.sdk.impl;

import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.password.PasswordPolicy;
import com.descope.model.user.User;
import com.descope.sdk.auth.PasswordService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PasswordFuncTest {

  private PasswordService passwordService;

  @BeforeEach
  void setUp() {
    var authParams = AuthParams.builder().projectId("P2OeA1IovZReU5JOxZ4oeC1U5MwX").build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.passwordService = AuthenticationServiceBuilder.buildServices(client, authParams).getPasswordService();
  }

  @Test
  void signUpForPassword() {
    User user = new User("krishna teja", "krishna.teja@apptware.com", "+918143887765");
    AuthenticationInfo signUp = passwordService.signUp("krishna.teja@apptware.com", user, "Test@12345");
    System.out.println(signUp);
  }

  @Test
  void signIn() {
    AuthenticationInfo signIn = passwordService.signIn("krishna.teja@apptware.com", "Test@12345");
    System.out.println(signIn);
  }

  @Test
  void testUpdateUserPassword() {
    passwordService.updateUserPassword("krishna.teja@apptware.com", "Test@123456");
  }

  @Test
  void testGetPolicy() {
    PasswordPolicy passwordPolicy = passwordService.getPasswordPolicy();
    System.out.println(passwordPolicy);
  }
}

