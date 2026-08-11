package com.descope.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthUtilsTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String TOKEN = "some-refresh-token";
  private static final String KEY = "some-management-key";

  @Test
  void testProjectIdOnly() {
    assertThat(AuthUtils.getBearerHeader(PROJECT_ID)).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(AuthUtils.getBearerHeader(PROJECT_ID, null, null)).isEqualTo("Bearer " + PROJECT_ID);
  }

  @Test
  void testProjectIdAndKey() {
    assertThat(AuthUtils.getBearerHeader(PROJECT_ID, null, KEY))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + KEY);
  }

  @Test
  void testProjectIdAndToken() {
    assertThat(AuthUtils.getBearerHeader(PROJECT_ID, TOKEN, null))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + TOKEN);
  }

  @Test
  void testAllParts() {
    assertThat(AuthUtils.getBearerHeader(PROJECT_ID, TOKEN, KEY))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + TOKEN + ":" + KEY);
  }

  @Test
  void testBlankPartsAreSkipped() {
    assertThat(AuthUtils.getBearerHeader(PROJECT_ID, "", "   ", KEY))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + KEY);
    assertThat(AuthUtils.getBearerHeader("", TOKEN)).isEqualTo("Bearer " + TOKEN);
  }

  @Test
  void testNoParts() {
    assertThat(AuthUtils.getBearerHeader()).isEqualTo("Bearer ");
    assertThat(AuthUtils.getBearerHeader(null, null)).isEqualTo("Bearer ");
  }
}
