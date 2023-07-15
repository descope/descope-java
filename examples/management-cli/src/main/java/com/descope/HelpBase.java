package com.descope;

import picocli.CommandLine.Option;

public abstract class HelpBase {
  @Option(names = { "-h", "--help"}, usageHelp = true, description = "show this help message and exit")
  boolean help;
}
