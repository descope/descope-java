package com.descope.model.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link VerifyOptions}. */
class VerifyOptionsTest {

  @Test
  void builderSingleAudienceConvenience() {
    VerifyOptions options = VerifyOptions.builder().audience("api.example.com").build();
    assertThat(options.getAudiences()).containsExactly("api.example.com");
    assertThat(options.getAudiencesOrEmpty()).containsExactly("api.example.com");
  }

  @Test
  void builderNullSingleAudience() {
    VerifyOptions options = VerifyOptions.builder().audience(null).build();
    assertThat(options.getAudiences()).isEmpty();
    assertThat(options.getAudiencesOrEmpty()).isEmpty();
  }

  @Test
  void builderMultipleAudiences() {
    List<String> audiences = Arrays.asList("a.example.com", "b.example.com");
    VerifyOptions options = VerifyOptions.builder().audiences(audiences).build();
    assertThat(options.getAudiences())
        .containsExactlyInAnyOrderElementsOf(audiences);
  }

  @Test
  void withAudienceStaticHelper() {
    VerifyOptions options = VerifyOptions.withAudience("only.example.com");
    assertThat(options.getAudiences()).containsExactly("only.example.com");
  }

  @Test
  void withAudiencesStaticHelper() {
    VerifyOptions options =
        VerifyOptions.withAudiences(Arrays.asList("a", "b", "c"));
    assertThat(options.getAudiences()).containsExactly("a", "b", "c");
  }

  @Test
  void defaultsSafeToRead() {
    VerifyOptions options = new VerifyOptions();
    assertThat(options.getAudiences()).isNull();
    assertThat(options.getAudiencesOrEmpty()).isEmpty();
  }

  @Test
  void emptyListIsNoOp() {
    VerifyOptions options =
        VerifyOptions.builder().audiences(Collections.emptyList()).build();
    assertThat(options.getAudiencesOrEmpty()).isEmpty();
  }
}
