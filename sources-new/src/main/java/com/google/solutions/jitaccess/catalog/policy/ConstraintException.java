package com.google.solutions.jitaccess.catalog.policy;

public abstract class ConstraintException extends Exception {
  public ConstraintException(String message) {
    super(message);
  }

  public ConstraintException(String message, Throwable cause) {
    super(message, cause);
  }
}
