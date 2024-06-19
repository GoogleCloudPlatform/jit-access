package com.google.solutions.jitaccess.catalog.policy;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface Policy {
  /**
   * @return parent policy, if any.
   */
  @NotNull Optional<Policy> parent();

  /**
   * @return ACL, if any. A policy without an ACL grants access to all principals.
   */
  @NotNull Optional<AccessControlList> accessControlList();

  /**
   * @return constraints, if any.
   */
  @NotNull Collection<Constraint> constraints(ConstraintClass action);

  public enum ConstraintClass {
    JOIN,
    APPROVE,
    RECERTIFY
  }
}
