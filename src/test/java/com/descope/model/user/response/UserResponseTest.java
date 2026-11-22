package com.descope.model.user.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
