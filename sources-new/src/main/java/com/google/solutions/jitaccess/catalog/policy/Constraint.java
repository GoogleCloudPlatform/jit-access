package com.google.solutions.jitaccess.catalog.policy;

import org.jetbrains.annotations.NotNull;

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
  @NotNull Map<String, Property> expectedInput();

  /**
   * @return a check object that can be used to evaluate
   * the constraint.
   */
  ConstraintCheck createCheck();
}
