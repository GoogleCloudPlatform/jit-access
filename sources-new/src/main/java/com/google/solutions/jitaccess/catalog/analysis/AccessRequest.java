package com.google.solutions.jitaccess.catalog.analysis;

import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.AccessRights;
import com.google.solutions.jitaccess.catalog.policy.Constraint;
import com.google.solutions.jitaccess.catalog.policy.ConstraintException;
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
  public boolean checkConstraint(Constraint c) throws ConstraintException {
    if (!c.requiredInput().keySet().stream().allMatch(k -> this.input().containsKey(k))) {
      throw new InsufficientInputException("One or more required input properties are missing");
    }

    var check = c.createCheck();

    //
    // Copy input properties.
    //
    var input = check.addContext("input");
    for (var entry : this.input().entrySet()) {
      input.set(entry.getKey(), entry.getValue().value());
    }

    //
    // Copy request properties.
    //
    var subject = check.addContext("subject");
    subject.set("email", this.subject().user().email);
    subject.set("principals", this.subject().principals()
      .stream()
      .map(p -> p.id().value())
      .toList());

    var group = check.addContext("group");
    group.set("environment", this.group().environment());
    group.set("system", this.group().system());
    group.set("name", this.group().name());

    return check.execute();
  }

  class InsufficientInputException extends ConstraintException {
    public InsufficientInputException(String message) {
      super(message);
    }
  }
}