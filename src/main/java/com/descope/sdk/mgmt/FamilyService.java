package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.family.Family;
import com.descope.model.family.FamilyDependent;
import com.descope.model.family.FamilySettings;
import com.descope.model.family.request.FamilyDependentRequest;
import com.descope.model.family.request.FamilySearchRequest;
import java.util.List;
import java.util.Map;

/** Provides functions for managing families and their dependents. */
public interface FamilyService {

  /**
   * Create a new family.
   *
   * @param name The family name.
   * @param customAttributes Optional custom attributes of the family.
   * @param photo Optional photo URL of the family.
   * @param disabled Optional, whether the family is disabled.
   * @return The created {@link Family}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Family create(String name, Map<String, Object> customAttributes, String photo, Boolean disabled)
      throws DescopeException;

  /**
   * Create a new family with the given ID.
   *
   * @param id The family ID to use.
   * @param name The family name.
   * @param customAttributes Optional custom attributes of the family.
   * @param photo Optional photo URL of the family.
   * @param disabled Optional, whether the family is disabled.
   * @return The created {@link Family}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Family createWithId(String id, String name, Map<String, Object> customAttributes, String photo,
      Boolean disabled) throws DescopeException;

  /**
   * Update an existing family. Fields left null keep their current value. When given, the custom
   * attributes replace all of the family's custom attributes.
   *
   * @param id The family ID.
   * @param name Optional new name.
   * @param customAttributes Optional new custom attributes.
   * @param photo Optional new photo URL.
   * @param disabled Optional, whether the family is disabled.
   * @return The updated {@link Family}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Family update(String id, String name, Map<String, Object> customAttributes, String photo, Boolean disabled)
      throws DescopeException;

  /**
   * Delete an existing family. This action is irreversible.
   *
   * @param id The family ID.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void delete(String id) throws DescopeException;

  /**
   * Search families.
   *
   * @param request Optional search options. Empty options return all families.
   * @return The matching families.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<Family> searchAll(FamilySearchRequest request) throws DescopeException;

  /**
   * Create a dependent user in a family.
   *
   * @param familyId The family to create the dependent in.
   * @param dependent Optional details of the dependent.
   * @return The created {@link FamilyDependent}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilyDependent createDependent(String familyId, FamilyDependentRequest dependent) throws DescopeException;

  /**
   * Delete a dependent user. The family is inferred from the dependent.
   *
   * @param userId The user ID of the dependent.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteDependent(String userId) throws DescopeException;

  /**
   * Impersonate a family dependent. The impersonator must be a member of the dependent's family and
   * hold the permission to impersonate dependents.
   *
   * @param impersonatorUserIdOrLoginId The impersonator, by user ID or login ID.
   * @param dependentLoginId The login ID of the dependent to impersonate.
   * @param selectedFamily Optional family to scope the session to; it must be the dependent's family.
   * @return The JWT of the impersonated dependent.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String impersonateDependent(String impersonatorUserIdOrLoginId, String dependentLoginId, String selectedFamily)
      throws DescopeException;

  /**
   * Stop a family dependent impersonation given the impersonated dependent's JWT.
   *
   * @param jwt The JWT of the impersonated dependent.
   * @param customClaims Optional custom claims to add to the returned JWT.
   * @param refreshDuration Optional refresh duration of the returned JWT.
   * @return A new JWT for the impersonator.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String stopImpersonation(String jwt, Map<String, Object> customClaims, Integer refreshDuration)
      throws DescopeException;

  /**
   * Get the project's family settings.
   *
   * @return The {@link FamilySettings}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilySettings getSettings() throws DescopeException;

  /**
   * Configure the project's family settings. Fields left null keep their current value.
   *
   * @param settings The settings to change.
   * @return The resulting {@link FamilySettings}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FamilySettings configureSettings(FamilySettings settings) throws DescopeException;
}
