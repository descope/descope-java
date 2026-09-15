package com.descope.exception;

/**
 * Thrown when an update cannot be applied because the new identifier already belongs to another
 * user and the caller asked to fail instead of merging, by passing {@code failOnConflict}.
 *
 * <p>Without {@code failOnConflict} the server merges the two users and deletes the other one,
 * so catching this exception is the only way to be told about the collision.
 */
public class UserConflictException extends DescopeException {

  public UserConflictException(String message, String code) {
    super(message);
    setCode(code);
  }
}
