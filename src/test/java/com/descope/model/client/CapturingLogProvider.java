package com.descope.model.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

// Records WARN messages of every logger so tests can assert on them.
public class CapturingLogProvider implements SLF4JServiceProvider {

  static final List<String> WARNINGS = new CopyOnWriteArrayList<>();

  private final IMarkerFactory markerFactory = new BasicMarkerFactory();
  private final MDCAdapter mdcAdapter = new NOPMDCAdapter();
  private final ILoggerFactory loggerFactory = name -> new CapturingLogger(name);

  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  @Override
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  @Override
  public MDCAdapter getMDCAdapter() {
    return mdcAdapter;
  }

  @Override
  public String getRequestedApiVersion() {
    return "2.0.99";
  }

  @Override
  public void initialize() {
    // Nothing to set up.
  }

  private static final class CapturingLogger extends LegacyAbstractLogger {
    private static final long serialVersionUID = 1L;

    private CapturingLogger(String name) {
      this.name = name;
    }

    @Override
    public boolean isTraceEnabled() {
      return false;
    }

    @Override
    public boolean isDebugEnabled() {
      return false;
    }

    @Override
    public boolean isInfoEnabled() {
      return false;
    }

    @Override
    public boolean isWarnEnabled() {
      return true;
    }

    @Override
    public boolean isErrorEnabled() {
      return false;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
      return Logger.class.getName();
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String msg, Object[] args,
        Throwable throwable) {
      if (level == Level.WARN) {
        WARNINGS.add(MessageFormatter.arrayFormat(msg, args).getMessage());
      }
    }
  }
}
