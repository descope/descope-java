package com.descope.sdk.auth.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.SSOServiceProvider;
import java.net.URI;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class SSOServiceProviderImplTest {

  private final SSOServiceProvider ssoService = AuthenticationServiceBuilder.buildServices(Client.builder()
      .uri("https://api.descope.com")
      .projectId("P123456789012345678901234567")
      .sdkInfo(SdkInfo.builder().name("java").build())
      .build()).getSsoServiceProvider();

  @Test
  void testStartPreservesExistingOverloads() {
    LoginOptions options = new LoginOptions();

    URI uri = captureStart(() -> ssoService.start("tenant-id", "https://app.example/callback", "login", options),
        options);
    assertThat(uri.getPath()).isEqualTo("/v1/auth/sso/authorize");
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "redirectURL=https%3A%2F%2Fapp.example%2Fcallback",
        "prompt=login").doesNotContain("ssoId=", "loginHint=");

    uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "refresh-token"), options);
    assertThat(uri.getRawQuery()).isEqualTo("tenant=tenant-id");
  }

  @Test
  void testStartWithSsoIdAndLoginHint() {
    LoginOptions options = new LoginOptions();
    URI uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "connection/id",
        "user+name@example.com"), options);

    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "ssoId=connection%2Fid",
        "loginHint=user%2Bname%40example.com");
    assertThat(uri.getRawQuery()).doesNotContain("redirectURL=", "prompt=");
  }

  @Test
  void testStartWithRefreshTokenAndOptionalParameters() {
    LoginOptions options = LoginOptions.builder().stepup(true).build();
    URI uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "refresh-token",
        "connection-id", "  "), options);
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "ssoId=connection-id")
        .doesNotContain("loginHint=");

    uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "refresh-token",
        "  ", "user@example.com"), options);
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "loginHint=user%40example.com")
        .doesNotContain("ssoId=");
  }

  private URI captureStart(Supplier<String> call, LoginOptions options) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse("https://idp.example/login")).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThat(call.get()).isEqualTo("https://idp.example/login");
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(options), eq(OAuthResponse.class));
    return uri.getValue();
  }
}
