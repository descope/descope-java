package com.descope;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.audit.AuditSearchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "audit-search", description = "Search audit trail")
public class AuditSearch extends HelpBase implements Callable<Integer>{
  
  @Option(names = { "-f", "--from"}, description = "Search last number of days")
  int from;

  @Option(names = { "-t", "--to"}, description = "Search up to number of days")
  int to;

  @Option(names = { "-x", "--text"}, description = "Search for text in relevant fields")
  String text;


  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var auditService = client.getManagementServices().getAuditService();
      var builder = AuditSearchRequest.builder();
      if (from != 0) {
        builder = builder.from(Instant.now().minus(Duration.ofDays(from)));
      }
      if (to != 0) {
        builder = builder.to(Instant.now().minus(Duration.ofDays(from)));
      }
      if (StringUtils.isNotBlank(text)) {
        builder = builder.text(text);
      }
      var res = auditService.search(builder.build());
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res));
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
