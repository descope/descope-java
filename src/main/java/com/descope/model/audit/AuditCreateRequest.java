package com.descope.model.audit;

import com.descope.enums.AuditType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditCreateRequest {
  String userId;
  String action;
  AuditType type;
  String actorId;
  String tenantId;
  Map<String, Object> data;
}
