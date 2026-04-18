package com.descope.model.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for verifying a Descope-issued JWT.
 *
 * <p>Mirrors the {@code VerifyOptions} type used by the Descope Node SDK (see
 * <a href="https://github.com/descope/node-sdk/blob/main/lib/types.ts">node-sdk/lib/types.ts</a>).
 *
 * <p>Currently supports verifying the {@code aud} (audience) claim on the JWT. When one or more
 * expected audience values are provided, validation succeeds only if the token's audience claim
 * contains at least one of the expected values. This matches the behavior of
 * {@code jwtVerify(..., { audience })} in the Node SDK, where {@code audience} may be a string or
 * an array of strings.
 *
 * <p>Typical use:
 * <pre>{@code
 * VerifyOptions options = VerifyOptions.builder().audience("my-api").build();
 * Token token = authenticationService.validateSessionWithToken(jwt, options);
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOptions {

  /**
   * List of acceptable {@code aud} (audience) values. Validation succeeds if the token's audience
   * claim contains any of these values. If this list is {@code null} or empty, audience validation
   * is skipped (matching the Node SDK behavior).
   */
  private List<String> audiences;

  /**
   * Construct {@link VerifyOptions} with a single expected audience.
   *
   * @param audience the required audience value
   * @return new {@code VerifyOptions} requiring the given audience
   */
  public static VerifyOptions withAudience(String audience) {
    return VerifyOptions.builder().audience(audience).build();
  }

  /**
   * Construct {@link VerifyOptions} that accept any of several audience values.
   *
   * @param audiences list of acceptable audience values
   * @return new {@code VerifyOptions} requiring one of the given audiences
   */
  public static VerifyOptions withAudiences(List<String> audiences) {
    return VerifyOptions.builder().audiences(audiences).build();
  }

  /**
   * Returns the configured expected audiences, or an empty list if none were set.
   *
   * @return never-null list of acceptable audience values
   */
  public List<String> getAudiencesOrEmpty() {
    return audiences == null ? Collections.emptyList() : audiences;
  }

  /**
   * Convenience builder extension that accepts a single audience value.
   */
  public static class VerifyOptionsBuilder {
    /**
     * Convenience setter for a single expected audience. The token must contain this value in its
     * {@code aud} claim.
     *
     * @param audience the required audience value
     * @return this builder
     */
    public VerifyOptionsBuilder audience(String audience) {
      this.audiences =
          audience == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(audience));
      return this;
    }
  }
}
