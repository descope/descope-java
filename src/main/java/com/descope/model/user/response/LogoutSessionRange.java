package com.descope.model.user.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A time range of session creation times whose sessions are logged out.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutSessionRange {
  private String id;
  /**
   * Sessions created after this time are logged out. This is what the server stored, which may be later
   * than the requested after (or set when none was requested).
   */
  private Instant after;
  /** Sessions created before this time are logged out. */
  private Instant before;
  /** The session types this range applies to, empty for all session types. */
  private List<String> sessionTypes;
}
