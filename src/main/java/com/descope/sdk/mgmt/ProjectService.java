package com.descope.sdk.mgmt;

import com.descope.enums.ProjectEnvironment;
import com.descope.enums.ProjectTag;
import com.descope.exception.DescopeException;
import com.descope.model.project.ExportProjectResponse;
import com.descope.model.project.ExportSnapshotRequest;
import com.descope.model.project.ExportSnapshotResponse;
import com.descope.model.project.ImportSnapshotRequest;
import com.descope.model.project.NewProjectResponse;
import com.descope.model.project.Project;
import com.descope.model.project.ValidateSnapshotResponse;
import java.util.List;
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
   * Clone the current project, including its settings and configurations
   * - This action is supported only with a pro license or above.
   * - Users, tenants and access keys are not cloned.
   *
   * @param name The new name for the project
   * @param environment The environment for the new project, or {@code null} for none
   * @param tags The tags for the new project, or {@code null} for none
   * @return {@link NewProjectResponse NewProjectResponse}
   * @throws DescopeException - error upon failure
   */
  NewProjectResponse cloneProject(String name, ProjectEnvironment environment, List<String> tags)
      throws DescopeException;

  /**
   * Exports all settings and configurations for a project and returns the
   * raw JSON files response as an object.
   *  - This action is supported only with a pro license or above.
   *  - Users, tenants and access keys are not cloned.
   *  - Secrets, keys and tokens are not stripped from the exported data.
   *
   * @return An object containing the exported JSON files payload.
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

  /**
   * Exports a snapshot of all the settings and configurations for the current project.
   *  - This action is supported only with a pro license or above.
   *  - Users, tenants and access keys are not exported.
   *  - Secrets, keys and tokens are not included in the snapshot.
   *
   * @param request The export options, or {@code null} for the defaults
   * @return {@link ExportSnapshotResponse ExportSnapshotResponse} with the snapshot files
   * @throws DescopeException - error upon failure
   */
  ExportSnapshotResponse exportSnapshot(ExportSnapshotRequest request) throws DescopeException;

  /**
   * Imports a snapshot of all settings and configurations into the current project,
   * overriding any current configuration.
   *  - This action is supported only with a pro license or above.
   *  - Secrets the snapshot needs are provided in the request's {@code inputSecrets}.
   *
   * @param request The snapshot files, as returned by {@link #exportSnapshot}, and the import options
   * @throws DescopeException - error upon failure
   */
  void importSnapshot(ImportSnapshotRequest request) throws DescopeException;

  /**
   * Validates a snapshot before importing it into the current project, and reports
   * any failures or missing secrets.
   *  - This action is supported only with a pro license or above.
   *
   * @param request The snapshot files and options, as they would be passed to {@link #importSnapshot}
   * @return {@link ValidateSnapshotResponse ValidateSnapshotResponse}
   * @throws DescopeException - error upon failure
   */
  ValidateSnapshotResponse validateSnapshot(ImportSnapshotRequest request) throws DescopeException;

  /**
   * Update the current project tags.
   *
   * @param tags The new tags for the project
   * @throws DescopeException - error upon failure
   */
  void updateTags(List<String> tags) throws DescopeException;

  /**
   * Delete the current project.
   *
   * @throws DescopeException - error upon failure
   */
  void deleteProject() throws DescopeException;

  /**
   * List all projects in the current company.
   *
   * @return the list of projects
   * @throws DescopeException - error upon failure
   */
  List<Project> listProjects() throws DescopeException;
}
