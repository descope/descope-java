package com.descope;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "management-cli", subcommands = {
  UserCreate.class,
  UserUpdate.class,
  UserDelete.class,
  UserLoad.class,
  UserSearch.class,
  AccessKeyCreate.class,
  AccessKeyUpdate.class,
  AccessKeyDelete.class,
  AccessKeyLoad.class,
  AccessKeySearch.class,
  TenantCreate.class,
  TenantLoad.class,
  TenantUpdate.class,
  TenantDelete.class,
  PermissionCreate.class,
  PermissionUpdate.class,
  PermissionDelete.class,
  PermissionLoadAll.class,
  RoleCreate.class,
  RoleDelete.class,
  RoleUpdate.class,
  RoleLoadAll.class,
  AuditSearch.class,
  AuditCreate.class
})
public class ManagementCLI implements Callable<Integer> {
  @Option(names = { "-h", "--help"}, usageHelp = true, description = "show this help message and exit")
  boolean help;

  // dummy comment
  static CommandLine cli;

  @Override
  public Integer call() {
    cli.usage(System.out);
    return 0;
  }

  public static void main(String[] args) {
    cli = new CommandLine(new ManagementCLI());
    int exitCode = cli.execute(args);
    System.exit(exitCode);
  }
}
