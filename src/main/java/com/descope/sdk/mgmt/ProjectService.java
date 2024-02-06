package com.descope.sdk.mgmt;

import com.descope.enums.ProjectTag;
import com.descope.exception.DescopeException;
import com.descope.model.project.ExportProjectResponse;
import com.descope.model.project.NewProjectResponse;
import java.util.Map;

public interface ProjectService {
  /**
   * Update the current project name.
   *
   * @param name The new name for the project
   * @throws DescopeException - error upon failure
   */
  void updateName(String name) throws DescopeException;

  /**
   * Clone the current project, including its settings and configurations
   * - This action is supported only with a pro license or above.
   * - Users, tenants and access keys are not cloned.
   *
   * @param name The new name for the project
   * @param tag The tag for the project
   * @return {@link NewProjectResponse NewProjectResponse}
   * @throws DescopeException - error upon failure
   */
  NewProjectResponse cloneProject(String name, ProjectTag tag) throws DescopeException;

  /**
   * Exports all settings and configurations for a project and returns the
   * raw JSON files response as an object.
   *  - This action is supported only with a pro license or above.
   *  - Users, tenants and access keys are not cloned.
   *  - Secrets, keys and tokens are not stripped from the exported data.
   *
   * @returns An object containing the exported JSON files payload.
   * @throws DescopeException - error upon failure
   */
  ExportProjectResponse exportProject() throws DescopeException;

  /**
   * Imports all settings and configurations for a project overriding any
   * current configuration.
   *  - This action is supported only with a pro license or above.
   *  - Secrets, keys and tokens are not overwritten unless overwritten in the input.
   *
   * @param files The raw JSON dictionary of files, in the same format as the one returned by calls to export.
   */
  void importProject(Map<String, Object> files) throws DescopeException;
}
