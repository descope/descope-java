package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;


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
}
