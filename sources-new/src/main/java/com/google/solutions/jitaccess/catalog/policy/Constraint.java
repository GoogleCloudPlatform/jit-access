package com.google.solutions.jitaccess.catalog.policy;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * A constraint that a request, approval, or activation must satisfy.
 */
public interface Constraint {
  /**
   * @return unique name.
   */
  @NotNull String name();

  /**
   * @return display name.
   */
  @NotNull String displayName();

  /**
   * @return input properties required to perform a check.
   */
  @NotNull Collection<Property> expectedInput();

  /**
   * @return a check object that can be used to evaluate
   * the constraint.
   */
  Check createCheck();

  /**
   * Represents the evaluation of a constraint.
   */
  interface Check {
    /**
     * Add additional input that might be needed to
     * perform the check.
     */
    Context addContext(@NotNull String name);

    /**
     * Perform the actual check, taking all additional
     * input into account.
     */
    boolean execute() throws ConstraintException;
  }

  interface  Context {
    Context set(@NotNull String name, @NotNull Object val);
  }
}
