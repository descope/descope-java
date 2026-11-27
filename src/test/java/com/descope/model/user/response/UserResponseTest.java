package com.descope.model.user.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class UserResponseTest {

  private final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Test
  public void testDeserializeApiKeys() throws JsonProcessingException {
    UserResponse user =
        mapper.readValue(
            "{\"userId\":\"U1\",\"TOTP\":true,\"SAML\":true,\"OAuth\":{\"google\":true}}",
            UserResponse.class);
    assertEquals("U1", user.getUserId());
    assertEquals(Boolean.TRUE, user.getTOTP());
    assertEquals(Boolean.TRUE, user.getSAML());
    assertEquals(Collections.singletonMap("google", true), user.getOAuth());
  }

  @Test
  public void testDeserializeLowercaseKeys() throws JsonProcessingException {
    UserResponse user =
        mapper.readValue(
            "{\"totp\":true,\"saml\":true,\"oauth\":{\"google\":true}}", UserResponse.class);
    assertEquals(Boolean.TRUE, user.getTOTP());
    assertEquals(Boolean.TRUE, user.getSAML());
    assertEquals(Collections.singletonMap("google", true), user.getOAuth());
  }

  @Test
  public void testDeserializeMeFields() throws JsonProcessingException {
    UserResponse user =
        mapper.readValue(
            "{\"externalIds\":[\"e1\"],\"webauthn\":true,\"password\":true,\"editable\":true,"
                + "\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,"
                + "\"consentExpiration\":11,\"recoveryEmail\":\"r@x.com\",\"verifiedRecoveryEmail\":true,"
                + "\"recoveryPhone\":\"+1555\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":12,"
                + "\"roleIds\":[\"r1\"],\"recoveryCodes\":true,\"dependent\":true,"
                + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
                + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"t1\",\"samlTenantId\":\"t2\","
                + "\"oidcTenantId\":\"t3\",\"consentTenantId\":\"t4\",\"lastLoginTime\":13}",
            UserResponse.class);
    assertEquals(Arrays.asList("e1"), user.getExternalIds());
    assertEquals(Boolean.TRUE, user.getWebauthn());
    assertEquals(Boolean.TRUE, user.getPassword());
    assertEquals(Boolean.TRUE, user.getEditable());
    assertEquals(Boolean.TRUE, user.getSCIM());
    assertEquals(Boolean.TRUE, user.getPush());
    assertEquals(Arrays.asList("p1"), user.getPermissions());
    assertEquals(Boolean.TRUE, user.getOIDC());
    assertEquals(Long.valueOf(11), user.getConsentExpiration());
    assertEquals("r@x.com", user.getRecoveryEmail());
    assertEquals(Boolean.TRUE, user.getVerifiedRecoveryEmail());
    assertEquals("+1555", user.getRecoveryPhone());
    assertEquals(Boolean.TRUE, user.getVerifiedRecoveryPhone());
    assertEquals(Long.valueOf(12), user.getModifiedTime());
    assertEquals(Arrays.asList("r1"), user.getRoleIds());
    assertEquals(Boolean.TRUE, user.getRecoveryCodes());
    assertEquals(Boolean.TRUE, user.getDependent());
    assertEquals(1, user.getUserFamilies().size());
    UserFamily family = user.getUserFamilies().get(0);
    assertEquals("f1", family.getFamilyId());
    assertEquals(Arrays.asList("fr"), family.getRoleNames());
    assertEquals(Arrays.asList("fp"), family.getPermissions());
    assertEquals(Collections.singletonMap("a", "b"), family.getFamilyScopedAttributes());
    assertEquals("t1", user.getScimTenantId());
    assertEquals("t2", user.getSamlTenantId());
    assertEquals("t3", user.getOidcTenantId());
    assertEquals("t4", user.getConsentTenantId());
    assertEquals(Long.valueOf(13), user.getLastLoginTime());
  }

  @Test
  public void testSerializeKeysUnchanged() throws JsonProcessingException {
    UserResponse user = new UserResponse();
    user.setTOTP(true);
    user.setSAML(true);
    user.setOAuth(Collections.singletonMap("google", true));
    String json = mapper.writeValueAsString(user);
    assertTrue(json.contains("\"totp\":true"), json);
    assertTrue(json.contains("\"saml\":true"), json);
    assertTrue(json.contains("\"oauth\":{\"google\":true}"), json);
  }
}
