package com.descope.model.scim;

/** Values of {@code revokeMode}: how replaced or revoked SCIM keys are taken out of service. */
public final class ScimKeyRevokeMode {
  /** Deletes the keys permanently (the server default). */
  public static final String DELETE = "SCIM_KEY_REVOKE_MODE_DELETE";
  /** Disables the keys reversibly, so they can be restored with {@code activateKeys}. */
  public static final String DEACTIVATE = "SCIM_KEY_REVOKE_MODE_DEACTIVATE";

  private ScimKeyRevokeMode() {
  }
}
