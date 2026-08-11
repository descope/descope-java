package com.descope.utils;

import static com.descope.literals.AppConstants.BEARER_AUTHORIZATION_PREFIX;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class AuthUtils {

  /**
   * Builds an {@code Authorization} header value out of the given parts, joined with a colon and
   * prefixed with {@code Bearer }. Blank parts are skipped, so callers can pass optional values
   * without branching.
   *
   * <p>The canonical order is project ID, then a token (refresh JWT or access key), then a
   * management key, producing values such as {@code Bearer <projectId>},
   * {@code Bearer <projectId>:<managementKey>} or
   * {@code Bearer <projectId>:<refreshJwt>:<managementKey>}.
   *
   * @param parts the header parts, in order, any of which may be null or blank
   * @return the header value
   */
  public static String getBearerHeader(String... parts) {
    return BEARER_AUTHORIZATION_PREFIX + Arrays.stream(parts)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(":"));
  }
}
