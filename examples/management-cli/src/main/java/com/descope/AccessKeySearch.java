package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "access-key-search-all", description = "Search Descope access keys")
public class AccessKeySearch extends HelpBase implements Callable<Integer>{

  @Option(names = { "-t", "--tenant"}, description = "Optional tenant ids. Multiple tenants supported.")
  List<String> tenants;

  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var keyService = client.getManagementServices().getAccessKeyService();
      var res = keyService.searchAll(tenants);
      ObjectMapper objectMapper = new ObjectMapper();
      System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res));
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
