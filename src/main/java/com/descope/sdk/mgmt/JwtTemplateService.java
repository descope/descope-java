package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.jwttemplate.ApplyJwtTemplateFromLibraryRequest;
import com.descope.model.jwttemplate.JwtTemplate;
import com.descope.model.jwttemplate.JwtTemplateLibraryEntry;
import com.descope.model.jwttemplate.JwtTemplateValidationResponse;
import java.util.List;

/** Provides functions for managing JWT templates. */
public interface JwtTemplateService {

  /**
   * Create a JWT template.
   *
   * @param template the template to create, with a {@code name} and a {@code type}
   * @return the created template, with its server-assigned ID
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  JwtTemplate create(JwtTemplate template) throws DescopeException;

  /**
   * Update a JWT template.
   *
   * @param template the template to update, with its {@code id}, a {@code name} and a {@code type}
   * @return the updated template
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  JwtTemplate update(JwtTemplate template) throws DescopeException;

  /**
   * Load a JWT template.
   *
   * @param id the ID of the template to load
   * @return the template
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  JwtTemplate load(String id) throws DescopeException;

  /**
   * List all JWT templates of the project.
   *
   * @return the templates
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<JwtTemplate> list() throws DescopeException;

  /**
   * Validate a JWT template, either a stored one by its ID or the given template. A template that fails
   * validation is not an error: the response is not valid and lists the issues.
   *
   * @param id the ID of a stored template to validate, or {@code null}
   * @param template a template to validate, or {@code null}
   * @return whether the template is valid, and its issues
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  JwtTemplateValidationResponse validate(String id, JwtTemplate template) throws DescopeException;

  /**
   * List the starter JWT templates in the Descope library.
   *
   * @return the library entries
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<JwtTemplateLibraryEntry> listLibrary() throws DescopeException;

  /**
   * Load a starter JWT template from the Descope library.
   *
   * @param id the ID of the library entry
   * @return the library entry
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  JwtTemplateLibraryEntry loadLibraryEntry(String id) throws DescopeException;

  /**
   * Create a JWT template in the project from a Descope library entry.
   *
   * @param request the library entry ID, and optional overrides
   * @return the created template
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  JwtTemplate applyFromLibrary(ApplyJwtTemplateFromLibraryRequest request) throws DescopeException;
}
