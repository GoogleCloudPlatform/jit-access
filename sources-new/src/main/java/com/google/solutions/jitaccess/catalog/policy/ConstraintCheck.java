package com.google.solutions.jitaccess.catalog.policy;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the evaluation of a constraint.
 */
interface ConstraintCheck {
  /**
   * Add additional input that might be needed to
   * perform the check.
   */
  ConstraintCheck add(@NotNull String name, @NotNull Object val);

  /**
   * Perform the actual check, taking all additional
   * input into account.
   */
  boolean execute();
}