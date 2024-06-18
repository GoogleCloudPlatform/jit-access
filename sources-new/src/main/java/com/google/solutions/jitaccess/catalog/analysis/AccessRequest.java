package com.google.solutions.jitaccess.catalog.analysis;

import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Map;

public abstract class AccessRequest {
  public abstract @NotNull Subject subject();
  public abstract @NotNull JitGroupId groupId();
  protected abstract @NotNull EnumSet<PolicyRight> requiredRights();
  protected abstract @NotNull Policy effectivePolicy();

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
    if (!c.expectedInput().keySet().stream().allMatch(k -> this.input().containsKey(k))) {
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
    group.set("environment", this.groupId().environment());
    group.set("system", this.groupId().system());
    group.set("name", this.groupId().name());

    return check.execute();
  }

  public @NotNull AccessAnalysis analyze() { // TODO: test
    var policy = effectivePolicy();

    //
    // Check if the ACL permits the required access.
    //
    var aclAccessGranted = policy.acl().isAllowed(
      this.subject(),
      PolicyRight.toMask(requiredRights()));

    //
    // Check if the current user has the principal, i.e.,
    // has joined this group before.
    //
    var currentlyActive = this.subject()
      .principals()
      .stream()
      .filter(p -> p.isValid())
      .anyMatch(p -> p.id().equals(this.groupId()));

    var satisfiedConstraints = new LinkedList<Constraint>();
    var unsatisfiedConstraints = new LinkedList<Constraint>();
    for (var constraint : policy.constraints()) {
      try {
        if (checkConstraint(constraint)) {
          satisfiedConstraints.add(constraint);
        }
        else {
          unsatisfiedConstraints.add(constraint);
        }
      } catch (ConstraintException e) {
        unsatisfiedConstraints.add(constraint);
      }
    }

    return new AccessAnalysis(
      aclAccessGranted,
      currentlyActive,
      satisfiedConstraints,
      unsatisfiedConstraints);
  }

  class InsufficientInputException extends ConstraintException {
    public InsufficientInputException(String message) {
      super(message);
    }
  }
}