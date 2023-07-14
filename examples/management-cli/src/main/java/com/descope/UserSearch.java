package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.user.request.UserSearchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "user-search-all", description = "Search Descope users")
public class UserSearch extends HelpBase implements Callable<Integer> {

  @Option(names = { "-p", "--page" }, defaultValue = "0", description = "Page number of users to retrieve")
  int page;
  @Option(names = { "-l", "--limit" }, defaultValue = "0", description = "Max number of users. 0 is unlimited.")
  int limit;
  @Option(names = { "-t", "--test" }, description = "Retrieve test only users")
  boolean testOnly;
  @Option(names = "-tenant", description = "Tenants to search")
  List<String> tenants;

  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var userService = client.getManagementServices().getUserService();
      var res = userService.searchAll(
          UserSearchRequest.builder()
          .page(page)
          .limit(limit)
          .tenantIds(tenants)
          .build());
      ObjectMapper objectMapper = new ObjectMapper();
      System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res.getUsers()));
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
