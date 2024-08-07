package com.descope.model.audit;

import com.descope.enums.AuditType;
import java.time.Instant;
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
public class AuditRecord {
  String projectId;
  String userId;
  String actorId;
  AuditType type;
  String action;
  Instant occurred;
  String device;
  String method;
  String geo;
  String remoteAddress;
  List<String> loginIds;
  List<String> tenants;
  Map<String, Object> data;
}
