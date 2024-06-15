package com.google.solutions.jitaccess.catalog.policy;

import com.google.solutions.jitaccess.catalog.analysis.AccessRequest;

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

  boolean isApplicableTo(AccessRequest request);

  boolean isSatisfiedFor(AccessRequest request);

  // required input {CEL datatype, name}, like setting ("property")
}
