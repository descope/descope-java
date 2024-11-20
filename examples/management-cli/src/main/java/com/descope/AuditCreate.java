package com.descope;

import com.descope.client.DescopeClient;
import com.descope.enums.AuditType;
import com.descope.exception.DescopeException;
import com.descope.model.audit.AuditCreateRequest;
import com.descope.model.audit.AuditSearchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "audit-create", description = "Create an audit event")
public class AuditCreate extends HelpBase implements Callable<Integer> {
  
  @Option(names = { "-u", "--user"}, description = "User ID in the event")
  String userId;

  @Option(names = { "-i", "--actor"}, description = "Actor ID in the event")
  String actorId;

  @Option(names = { "-t", "--type"}, description = "event type (info, warn, error)")
  String type;

  @Option(names = { "-a", "--action"}, description = "the name of the action")
  String action;

  @Option(names = { "-n", "--tenant"}, description = "the tenant for the action")
  String tenant;

  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var builder = AuditCreateRequest.builder();
      if (StringUtils.isNotBlank(userId)) {
        builder.userId(userId);
      }
      if (StringUtils.isNotBlank(actorId)) {
        builder.actorId(actorId);
      }
      if (StringUtils.isNotBlank(type)) {
        builder.type(AuditType.fromString(type));
      }
      if (StringUtils.isNotBlank(action)) {
        builder.action(action);
      }
      if (StringUtils.isNotBlank(tenant)) {
        builder.tenantId(tenant);
      }
      var client = new DescopeClient();
      var auditService = client.getManagementServices().getAuditService();
      auditService.createEvent(builder.build());
      System.out.println("Done");
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
