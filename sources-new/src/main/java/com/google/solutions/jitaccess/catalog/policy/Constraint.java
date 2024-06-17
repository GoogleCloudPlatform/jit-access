package com.google.solutions.jitaccess.catalog.policy;

import com.google.solutions.jitaccess.catalog.analysis.AccessRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A constraint that a request, approval, or activation must satisfy.
 */
public interface Constraint {
  /**
   * Unique name for this constraint.
   * @return
   */
  @NotNull String name();

  /**
   * @return display name for the requirement.
   */
  @NotNull String displayName();

  /**
   * Prepare a check.
   */
  ConstraintCheck createCheck(@NotNull Map<String, Property> input);

  /**
   * @return input properties required to perform a check.
   */
  @NotNull Map<String, Property> requiredInput();
}
