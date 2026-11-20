package com.descope.model.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AssociatedTenantTest {

  @Test
  void testResponseOnlyFieldsAreNotSerialized() throws Exception {
    AssociatedTenant tenant = AssociatedTenant.builder()
        .tenantId("t1")
        .roleNames(Arrays.asList("r1"))
        .permissions(Arrays.asList("p1"))
        .roleIds(Arrays.asList("ri1"))
        .build();
    String json = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(tenant);
    assertEquals("{\"tenantId\":\"t1\",\"roleNames\":[\"r1\"]}", json);
  }
}
