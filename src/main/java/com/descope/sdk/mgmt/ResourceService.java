package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.resource.DynamicRegistrationTemplate;
import com.descope.model.resource.DynamicRegistrationTemplateRequest;
import com.descope.model.resource.LoadAllDynamicRegistrationTemplatesResponse;

// Provides functions for managing resources, such as dynamic registration templates, in a project.
public interface ResourceService {
  /**
   * Create a new dynamic registration template.
   *
   * @param request The template details, name is required
   * @return The created template
   * @throws DescopeException in case of errors
   */
  DynamicRegistrationTemplate createDynamicRegistrationTemplate(DynamicRegistrationTemplateRequest request)
      throws DescopeException;

  /**
   * Update an existing dynamic registration template. All fields are overridden.
   *
   * @param template The template to update, id and name are required
   * @return The updated template
   * @throws DescopeException in case of errors
   */
  DynamicRegistrationTemplate updateDynamicRegistrationTemplate(DynamicRegistrationTemplate template)
      throws DescopeException;

  /**
   * Load a dynamic registration template by ID.
   *
   * @param id The template ID, required
   * @return The template
   * @throws DescopeException in case of errors
   */
  DynamicRegistrationTemplate loadDynamicRegistrationTemplate(String id) throws DescopeException;

  /**
   * Load all dynamic registration templates in the project.
   *
   * @return The templates and their total count
   * @throws DescopeException in case of errors
   */
  LoadAllDynamicRegistrationTemplatesResponse loadAllDynamicRegistrationTemplates() throws DescopeException;
}
