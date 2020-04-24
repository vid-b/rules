package com.sap.iot.ain.rules.cron;

public class RuleCronParsingException extends Exception {

  private static final long serialVersionUID = 1L;

  public RuleCronParsingException() {
    super();
  }

  public RuleCronParsingException(String exception) {
    super(exception);
  }

  public RuleCronParsingException(String exception, Throwable cause) {
    super(exception, cause);
  }
}
