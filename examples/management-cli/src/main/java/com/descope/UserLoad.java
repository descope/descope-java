package com.descope;

import java.util.concurrent.Callable;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine.Command;

@Command(name = "user-load", description = "Load Descope user details")
public class UserLoad extends UserBase implements Callable<Integer>{

  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var userService = client.getManagementServices().getUserService();
      var res = userService.load(loginId);
      ObjectMapper objectMapper = new ObjectMapper();
      System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res.getUser()));
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
