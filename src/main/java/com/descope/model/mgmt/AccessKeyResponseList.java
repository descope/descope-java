package com.descope.model.mgmt;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessKeyResponseList {
  private List<AccessKeyResponseDetails> keys;
}
