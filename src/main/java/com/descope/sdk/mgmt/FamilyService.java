package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.model.family.Family;
import com.descope.model.family.FamilySettings;
import com.descope.model.family.request.FamilyDependentRequest;
import com.descope.model.family.request.FamilyRequest;
import com.descope.model.family.request.FamilySearchRequest;
import com.descope.model.family.response.FamilyCustomAttributesResponse;
import com.descope.model.user.response.UserResponse;
import java.util.List;
import java.util.Map;

/** Provides functions for managing families (family accounts) in a project. */
public interface FamilyService {

  /**
   * Create a new family. The family ID is generated automatically.
   *
   * @param request The family to create. The name is required (up to 254 characters).
   * @return The created {@link Family}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Family create(FamilyRequest request) throws DescopeException;

  /**
   * Create a new family with the given ID.
   *
   * @param id The family ID (up to 254 characters)
   * @param request The family to create. The name is required (up to 254 characters).
   * @return The created {@link Family}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Family createWithId(String id, FamilyRequest request) throws DescopeException;

  /**
   * Update an existing family. Only the fields that are set (non-null) are updated. Custom
   * attributes, when set, replace all of the family's custom attributes.
   *
   * @param id The family ID (up to 254 characters)
   * @param request The fields to update
   * @return The updated {@link Family}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Family update(String id, FamilyRequest request) throws DescopeException;

  /**
   * Delete an existing family. IMPORTANT: This action is irreversible. Use carefully.
   *
   * @param id The family ID (up to 254 characters)
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void delete(String id) throws DescopeException;

  /**
   * Search families. A null request returns all families.
   *
   * @param request The search options
   * @return The matching families
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<Family> searchAll(FamilySearchRequest request) throws DescopeException;

  /**
   * Create a dependent (shadow profile) user in a family, i.e. a user with no login credentials of
   * their own.
   *
   * @param familyId The family to create the dependent in (up to 254 characters)
   * @param dependent The dependent's details (optional)
   * @return The created user
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UserResponse createDependent(String familyId, FamilyDependentRequest dependent) throws DescopeException;

  /**
   * Delete a dependent user. The family is inferred from the dependent. IMPORTANT: This action is
   * irreversible. Use carefully.
   *
   * @param userId The dependent's user ID
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteDependent(String userId) throws DescopeException;

  /**
   * Impersonate a family dependent. The impersonator must be a member of the dependent's family and
   * hold the family impersonate-dependents permission there.
   *
   * @param impersonatorUserIdOrLoginId The impersonator's user ID or login ID (up to 100 characters)
   * @param dependentLoginId The dependent's login ID
   * @param selectedFamily Optional family to scope the impersonated session to (up to 254
   *     characters); when set it must be the dependent's family
   * @return The impersonated session JWT
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String impersonateDependent(String impersonatorUserIdOrLoginId, String dependentLoginId, String selectedFamily)
      throws DescopeException;

  /**
   * Stop impersonating a family dependent and return a JWT for the acting user's own session.
   *
   * @param jwt The impersonated session JWT
   * @param customClaims Custom claims to add to the returned JWT (optional)
   * @param refreshDuration Refresh duration of the returned JWT (optional)
   * @return The acting user's JWT
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String stopImpersonation(String jwt, Map<String, Object> customClaims, Integer refreshDuration)
      throws DescopeException;

  /**
   * Load the project's family account settings.
   *
   * @return {@link FamilySettings}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilySettings getSettings() throws DescopeException;

  /**
   * Update the project's family account settings. Only the fields that are set (non-null) are
   * updated.
   *
   * @param settings The settings to update
   * @return The settings after the change
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilySettings configureSettings(FamilySettings settings) throws DescopeException;

  /**
   * Get the family entity's custom attribute definitions.
   *
   * @return {@link FamilyCustomAttributesResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilyCustomAttributesResponse getCustomAttributes() throws DescopeException;

  /**
   * Create custom attribute definitions on the family entity.
   *
   * @param request The custom attributes to create. Each needs a name; display names are up to
   *     254 characters.
   * @return {@link FamilyCustomAttributesResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilyCustomAttributesResponse createCustomAttributes(CreateCustomAttributesRequest request) throws DescopeException;

  /**
   * Delete custom attribute definitions from the family entity by name.
   *
   * @param request The names of the attributes to delete
   * @return {@link FamilyCustomAttributesResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilyCustomAttributesResponse deleteCustomAttributes(DeleteCustomAttributesRequest request) throws DescopeException;
}
