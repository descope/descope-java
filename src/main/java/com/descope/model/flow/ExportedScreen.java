package com.descope.model.flow;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportedScreen {
  private String screenId;
  private List<Interaction> interactions;
  private Map<String, Object> contents;
}
