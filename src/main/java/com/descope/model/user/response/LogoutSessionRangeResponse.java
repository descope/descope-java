package com.descope.model.user.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Raw logout session range as returned by the server, with RFC 3339 times.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutSessionRangeResponse {
  String id;
  String after;
  String before;
  List<String> sessionTypes;
}
