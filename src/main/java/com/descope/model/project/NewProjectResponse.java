package com.descope.model.project;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewProjectResponse {
  private String projectId;
  private String projectName;
  private String tag;
  private String environment;
  private List<String> tags;

  public NewProjectResponse(String projectId, String projectName, String tag) {
    this(projectId, projectName, tag, null, null);
  }
}
