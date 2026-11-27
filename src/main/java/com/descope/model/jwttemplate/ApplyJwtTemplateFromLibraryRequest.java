package com.descope.model.jwttemplate;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Creates a JWT template from a library entry. Overrides left {@code null} keep the entry's values. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyJwtTemplateFromLibraryRequest {
  /** Required. */
  private String libraryEntryId;
  private String nameOverride;
  private String descriptionOverride;
  private List<String> tagsOverride;
  private Map<String, Object> templateOverride;
}
