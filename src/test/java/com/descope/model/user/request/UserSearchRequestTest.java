package com.descope.model.user.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
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
        .roleNames(Collections.singletonList("rolename1"))
        .build();

    String json = mapper.writeValueAsString(request);

    long expectedMillis = now.toEpochMilli();
    String expectedJson = String.format(
        "{\"tenantIds\":[\"tenant1\"],\"roles\":[\"role1\"],\"roleNames\":[\"rolename1\"],\"limit\":0,\"page\":0,"
        + "\"withTestUser\":null,"
        + "\"testUsersOnly\":null,\"customAttributes\":null,\"statuses\":null,\"emails\":null,\"phones\":null,"
        + "\"loginIds\":null,\"userIds\":null,\"ssoAppIds\":null,\"fromCreatedTime\":%d,\"toCreatedTime\":%d,"
        + "\"fromModifiedTime\":%d,\"toModifiedTime\":%d,"
        + "\"tenantRoleIds\":null,\"tenantRoleNames\":null,"
        + "\"lockReasons\":null,\"tempLockReasons\":null,\"text\":null}",
        expectedMillis, expectedMillis, expectedMillis, expectedMillis);

    assertEquals(expectedJson, json);
  }

  @Test
  public void testTenantRoleIdsAndNamesSerialization() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    UserSearchRequest request = UserSearchRequest.builder()
        .tenantIds(Collections.singletonList("tenant1"))
        .roles(Collections.singletonList("role1"))
        .roleNames(Collections.singletonList("rolename1"))
        .tenantRoleIds(Collections.singletonMap("tenant1",
            RolesList.builder().values(Collections.singletonList("roleA")).build()))
        .tenantRoleNames(Collections.singletonMap("tenant2",
            RolesList.builder().values(Collections.singletonList("roleX")).build()))
        .build();

    String json = mapper.writeValueAsString(request);
    String expectedJson = "{\"tenantIds\":[\"tenant1\"],\"roles\":[\"role1\"],\"roleNames\":[\"rolename1\"],"
        + "\"limit\":0,\"page\":0,"
        + "\"withTestUser\":null,\"testUsersOnly\":null,\"customAttributes\":null,\"statuses\":null,\"emails\":null,"
        + "\"phones\":null,\"loginIds\":null,\"userIds\":null,\"ssoAppIds\":null,\"fromCreatedTime\":null,"
        + "\"toCreatedTime\":null,\"fromModifiedTime\":null,\"toModifiedTime\":null,"
        + "\"tenantRoleIds\":{\"tenant1\":{\"values\":[\"roleA\"],\"and\":null}},"
        + "\"tenantRoleNames\":{\"tenant2\":{\"values\":[\"roleX\"],\"and\":null}},"
        + "\"lockReasons\":null,\"tempLockReasons\":null,\"text\":null}";

    assertEquals(expectedJson, json);
  }

  @Test
  public void testNewFieldsSerialization() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    UserSearchRequest request = UserSearchRequest.builder()
        .identifier("id1")
        .ssoOnly(true)
        .sort(Arrays.asList(SortField.builder().field("email").desc(true).build()))
        .scimOnly(false)
        .includeSubTenants(true)
        .password(true)
        .totp(false)
        .webauthn(true)
        .scim(false)
        .selectedColumns(Collections.singletonList("email"))
        .verifiedEmail(true)
        .verifiedPhone(false)
        .offset(5)
        .recoveryEmails(Collections.singletonList("a@b.com"))
        .recoveryPhones(Collections.singletonList("+15555555555"))
        .verifiedRecoveryEmail(true)
        .verifiedRecoveryPhone(false)
        .searchFields(Collections.singletonList(SearchField.builder().field("email").valStr("a%").build()))
        .fromLastLoginTime(Instant.ofEpochMilli(1743465600000L))
        .toLastLoginTime(Instant.ofEpochMilli(1746057600000L))
        .familyIds(Collections.singletonList("fam1"))
        .dependent(false)
        .build();

    String json = mapper.writeValueAsString(request);
    String expectedSuffix = "\"text\":null,\"identifier\":\"id1\",\"ssoOnly\":true,"
        + "\"sort\":[{\"field\":\"email\",\"desc\":true}],\"scimOnly\":false,\"includeSubTenants\":true,"
        + "\"password\":true,\"totp\":false,\"webauthn\":true,\"scim\":false,\"selectedColumns\":[\"email\"],"
        + "\"verifiedEmail\":true,\"verifiedPhone\":false,\"offset\":5,\"recoveryEmails\":[\"a@b.com\"],"
        + "\"recoveryPhones\":[\"+15555555555\"],\"verifiedRecoveryEmail\":true,\"verifiedRecoveryPhone\":false,"
        + "\"searchFields\":[{\"field\":\"email\",\"valStr\":\"a%\"}],"
        + "\"fromLastLoginTime\":\"1743465600000\",\"toLastLoginTime\":\"1746057600000\","
        + "\"familyIds\":[\"fam1\"],\"dependent\":false}";
    assertEquals(true, json.endsWith(expectedSuffix), json);
  }

  @Test
  public void testLegacyConstructorLeavesNewFieldsUnset() throws JsonProcessingException {
    UserSearchRequest request = new UserSearchRequest(null, null, null, 1, 2, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, "txt");
    assertEquals(Integer.valueOf(1), request.getLimit());
    assertEquals("txt", request.getText());
    String json = new ObjectMapper().writeValueAsString(request);
    assertEquals(true, json.endsWith("\"text\":\"txt\"}"), json);
  }
}