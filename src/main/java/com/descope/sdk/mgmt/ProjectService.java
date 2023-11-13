package com.descope.sdk.mgmt;

import com.descope.enums.ProjectTag;
import com.descope.exception.DescopeException;
import com.descope.model.project.NewProjectResponse;

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
  NewProjectResponse clone(String name, ProjectTag tag) throws DescopeException;
}
