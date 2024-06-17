package com.google.solutions.jitaccess.catalog.analysis;

import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.AccessRights;
import com.google.solutions.jitaccess.catalog.policy.Constraint;
import com.google.solutions.jitaccess.catalog.policy.Property;

import java.util.Map;

public abstract class AccessRequest {
  abstract Subject subject();

  abstract JitGroupId group();
  abstract AccessRights requiredRights();

  final void execute() {
    // check access, etc.
    // call executeCore
  }

  abstract void executeCore();

  abstract Map<String, Property> input();

  /**
   * Check if the request satisfies a constraint.
   */
  abstract boolean check(Constraint c);
}