package com.descope.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a trusted device of a user. Times are in seconds since the epoch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedDevice {
  String id;
  String name;
  String deviceType;
  String userId;
  Long lastLoginTime;
  Long expirationTime;
  String lastLocation;
  Boolean isCurrentDevice;
}
