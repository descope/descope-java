package com.descope;

import com.descope.client.DescopeClient;
import com.descope.enums.UserStatus;
import com.descope.exception.DescopeException;
import com.descope.model.user.request.UserSearchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
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
  @Option(names = { "-s", "--status" },
      description = "User statuses to search. Allows multiple flags. 'enabled'/'disabled'/'invited' are supported.")
  List<String> statuses;

  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var userService = client.getManagementServices().getUserService();
      List<UserStatus> statusesEnum = new ArrayList<>();
      if (statuses != null) {
        statusesEnum = statuses.stream().map(s -> UserStatus.valueOf(s.toUpperCase())).collect(Collectors.toList());
      }
      var req = UserSearchRequest.builder().page(page).limit(limit).tenantIds(tenants).statuses(statusesEnum).build();
      var res = userService.searchAll(req);
      ObjectMapper objectMapper = new ObjectMapper();
      System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res.getUsers()));
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
