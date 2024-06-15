package com.google.solutions.jitaccess.core.policy.constraints;

import com.google.solutions.jitaccess.core.access.Request;

/**
 * A constraint that a request, approval, or activation must satisfy.
 *
 * Implementing classes must support JSON serialization as
 * objects might be exposed by the API.
 */
public interface Constraint {
  /**
   * Unique name for this constraint.
   * @return
   */
  String name();

  /**
   * @return display name for the requirement.
   */
  String displayName();

  boolean isApplicableTo(Request request);

  boolean isSatisfiedFor(Request request);

  // required input {CEL datatype, name}, like setting ("property")
}
