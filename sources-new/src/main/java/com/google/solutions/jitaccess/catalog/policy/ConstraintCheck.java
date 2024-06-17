package com.google.solutions.jitaccess.catalog.policy;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the evaluation of a constraint.
 */
public interface ConstraintCheck {
  /**
   * Add additional input that might be needed to
   * perform the check.
   */
  Context add(@NotNull String name);

  /**
   * Perform the actual check, taking all additional
   * input into account.
   */
  boolean execute() throws ConstraintException;

  interface  Context {
    Context add(@NotNull String name, @NotNull Object val);
  }
}