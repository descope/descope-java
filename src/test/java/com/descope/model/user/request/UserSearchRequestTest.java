package com.descope.model.user.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.Test;


public class UserSearchRequestTest {

  @Test
  public void testInstantSerialization() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Instant now = Instant.now();
    UserSearchRequest request = UserSearchRequest.builder()
        .fromCreatedTime(now)
        .toCreatedTime(now)
        .fromModifiedTime(now)
        .toModifiedTime(now)
        .tenantIds(Collections.singletonList("tenant1"))
        .roles(Collections.singletonList("role1"))
        .build();

    String json = mapper.writeValueAsString(request);

    long expectedMillis = now.toEpochMilli();
    String expectedJson = String.format(
        "{\"tenantIds\":[\"tenant1\"],\"roles\":[\"role1\"],\"limit\":0,\"page\":0,\"withTestUser\":null,"
        + "\"testUsersOnly\":null,\"customAttributes\":null,\"statuses\":null,\"emails\":null,\"phones\":null,"
        + "\"loginIds\":null,\"ssoAppIds\":null,\"fromCreatedTime\":%d,\"toCreatedTime\":%d,\"fromModifiedTime\":%d,"
        + "\"toModifiedTime\":%d}", expectedMillis, expectedMillis, expectedMillis, expectedMillis);

    assertEquals(expectedJson, json);
  }
}