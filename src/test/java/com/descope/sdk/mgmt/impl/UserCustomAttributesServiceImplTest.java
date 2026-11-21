package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.CustomAttribute;
import com.descope.model.customattributes.CustomAttributeOption;
import com.descope.model.customattributes.CustomAttributesResponse;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.UserCustomAttributesService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;


public class UserCustomAttributesServiceImplTest {

  private UserCustomAttributesService userCustomAttributesService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.userCustomAttributesService = ManagementServiceBuilder.buildServices(client)
            .getUserCustomAttributesService();
  }

  @Test
  void testInvalidRequests() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
                    () -> userCustomAttributesService.createCustomAttributes(null));
    assertNotNull(thrown);
    assertEquals("The Request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
                    () -> userCustomAttributesService.createCustomAttributes(CreateCustomAttributesRequest
                                    .builder()
                                    .attributes(Arrays.asList(
                                                    CustomAttribute.builder().name("").type(1).build()))
                                    .build()));
    assertNotNull(thrown);
    assertEquals("The attribute name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
                    () -> userCustomAttributesService.createCustomAttributes(CreateCustomAttributesRequest
                                    .builder()
                                    .attributes(Arrays.asList(
                                                    CustomAttribute.builder().name("test").type(0).build()))
                                    .build()));
    assertNotNull(thrown);
    assertEquals("The attribute type argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
                    () -> userCustomAttributesService.createCustomAttributes(CreateCustomAttributesRequest
                                    .builder()
                                    .attributes(Arrays.asList(CustomAttribute.builder().name("test").type(1)
                                                    .options(Arrays.asList(CustomAttributeOption.builder()
                                                                    .label("a").build()))
                                                    .build()))
                                    .build()));
    assertNotNull(thrown);
    assertEquals("The attribute options argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
                    () -> userCustomAttributesService.createCustomAttributes(CreateCustomAttributesRequest
                                    .builder()
                                    .attributes(Arrays.asList(
                                                    CustomAttribute.builder().name("test").type(4).build()))
                                    .build()));
    assertNotNull(thrown);
    assertEquals("The attribute options argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetCustomAttributesTotalAboveIntMax() throws Exception {
    // int64 arrives as a quoted string
    CloseableHttpClient httpClient = mockHttpClient(
        "{\"data\":[{\"name\":\"a\",\"type\":1}],\"total\":\"3000000000\"}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      CustomAttributesResponse res = userCustomAttributesService.getCustomAttributes();
      assertEquals(1, res.getData().size());
      assertEquals(Integer.MAX_VALUE, res.getTotal());
      assertEquals(3000000000L, res.getTotalLong());
    }
  }

  @Test
  void testCustomAttributesResponseTotalAccessors() {
    CustomAttributesResponse res = new CustomAttributesResponse(null, 5);
    assertEquals(5, res.getTotal());
    assertEquals(5L, res.getTotalLong());
    res.setTotalLong(3000000000L);
    assertEquals(Integer.MAX_VALUE, res.getTotal());
    assertEquals(3000000000L, res.getTotalLong());
    res.setTotal(7);
    assertEquals(7L, res.getTotalLong());
    assertEquals(7, CustomAttributesResponse.builder().total(7).build().getTotal());
    assertEquals(3000000000L, CustomAttributesResponse.builder().totalLong(3000000000L).build().getTotalLong());
    assertNotEquals(CustomAttributesResponse.builder().totalLong(3000000000L).build(),
        CustomAttributesResponse.builder().totalLong(4000000000L).build());
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String name = TestUtils.getRandomName("ca").substring(0, 20);
    CustomAttribute ca = CustomAttribute.builder().name(name).type(1).build();
    CreateCustomAttributesRequest cas = CreateCustomAttributesRequest.builder()
                    .attributes(Arrays.asList(ca)).build();
    CustomAttributesResponse casRes = userCustomAttributesService.createCustomAttributes(cas);
    assertTrue(casRes.getTotal() > 0);
    boolean found = false;
    for (CustomAttribute tmp : casRes.getData()) {
      if (tmp.getName().equals(name)) {
        found = true;
        break;
      }
    }
    assertTrue(found);

    casRes = userCustomAttributesService.createCustomAttributes(cas);
    assertTrue(casRes.getTotal() > 0);

    casRes = userCustomAttributesService.deleteCustomAttributes(
                    DeleteCustomAttributesRequest.builder().names(Arrays.asList(name)).build());
    found = false;
    for (CustomAttribute tmp : casRes.getData()) {
      if (tmp.getName().equals(name)) {
        found = true;
        break;
      }
    }
    assertFalse(found);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CloseableHttpClient mockHttpClient(String responseBody) throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(200);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent())
          .thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(any(), any(HttpClientResponseHandler.class));
    return httpClient;
  }
}
