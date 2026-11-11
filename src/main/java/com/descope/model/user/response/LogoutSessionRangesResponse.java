package com.descope.model.user.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for the response of listing the active logout session ranges.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutSessionRangesResponse {
  List<LogoutSessionRangeResponse> ranges;
}
