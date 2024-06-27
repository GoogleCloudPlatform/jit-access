//
// Copyright 2024 Google LLC
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.google.solutions.jitaccess.catalog.policy;

import com.google.common.base.Preconditions;
import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Principal;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AccessCheck {
  private final @NotNull Subject subject;
  private final @NotNull JitGroupId groupId;
  private final @NotNull Policy policy;
  private final @NotNull EnumSet<PolicyRight> requiredRights;

  private final @Nullable LinkedList<Constraint.Check> constraintChecks = new LinkedList<>();

  private boolean executed = false;

  AccessCheck(
    @NotNull Policy policy,
    @NotNull Subject subject,
    @NotNull JitGroupId groupId,
    @NotNull EnumSet<PolicyRight> requiredRights
  ) {
    Preconditions.checkArgument(
      !requiredRights.isEmpty(),
      "At least one right must be specified");

    this.subject = subject;
    this.policy = policy;
    this.groupId = groupId;
    this.requiredRights = requiredRights;
  }

  private void evaluateConstraintCheck(
    @NotNull Constraint.Check check,
    @NotNull Result resultAccumulator
  ) {
    //
    // Copy request properties.
    //
    var subject = check.addContext("subject");
    subject.set("email", this.subject.user().email);
    subject.set("principals", this.subject.principals()
      .stream()
      .map(p -> p.id().value())
      .toList());

    var group = check.addContext("group");
    group.set("environment", this.groupId.environment());
    group.set("system", this.groupId.system());
    group.set("name", this.groupId.name());

    try {
      if (check.execute()) {
        resultAccumulator.satisfiedConstraints.add(check.constraint());
      }
      else {
        resultAccumulator.unsatisfiedConstraints.add(check.constraint());
      }
    } catch (Exception e) {
      resultAccumulator.unsatisfiedConstraints.add(check.constraint());
      resultAccumulator.failedConstraints.put(check.constraint(), e);
    }
  }

  /**
   * Add a set of constraints to be considered.
   */
  public @NotNull AccessCheck applyConstraints(
    @NotNull Policy.ConstraintClass constraintClass
  ) {
    this.constraintChecks.addAll(
      this.policy.constraints(constraintClass)
        .stream()
        .map(c -> c.createCheck())
        .toList());

    return this;
  }

  /**
   * @return input required to evaluate constraints.
   */
  public @NotNull List<Property> input() {
    return this.constraintChecks.stream()
      .flatMap(c -> c.input().stream())
      .toList();
  }

  public Result execute() {
    if (this.executed) {
      throw new IllegalStateException("The check was executed already");
    }

    this.executed = true;

    //
    // Evaluate ACL and constraints of the policy hierarchy.
    //
    var result = new Result(policy.checkAccess(
      this.subject,
      this.requiredRights));

    for (var constraintCheck : this.constraintChecks) {
      evaluateConstraintCheck(constraintCheck, result);
    }

    //
    // Check if the current user has the principal, i.e.,
    // has joined this group before. We only need to do this
    // once as it doesn't depend on the policy.
    //
    result.activeMembership = this.subject
      .principals()
      .stream()
      .filter(p -> p.isValid() && p.id().equals(this.groupId))
      .findFirst();

    assert result.failedConstraints
      .keySet()
      .stream()
      .allMatch(c -> result.unsatisfiedConstraints.contains(c));

    return result;
  }

  public class Result {
    private final boolean isSubjectInAcl;
    private Optional<Principal> activeMembership;
    private @NotNull LinkedList<Constraint> satisfiedConstraints;
    private @NotNull LinkedList<Constraint> unsatisfiedConstraints;
    private @NotNull Map<Constraint, Exception> failedConstraints;

    private Result(boolean isSubjectInAcl) {
      this.isSubjectInAcl = isSubjectInAcl;
      this.activeMembership = Optional.empty();
      this.satisfiedConstraints = new LinkedList<>();
      this.unsatisfiedConstraints = new LinkedList<>();
      this.failedConstraints = new HashMap<>();
    }

    public Collection<Constraint> satisfiedConstraints() {
      return satisfiedConstraints;
    }

    public Collection<Constraint> unsatisfiedConstraints() {
      return unsatisfiedConstraints;
    }

    /**
     * Input used to perform check.
     */
    public @NotNull List<Property> input() {
      return AccessCheck.this.input();
    }

    /**
     * @return failed constraints and the exception they encountered.
     *
     * Failed constraints are always unsatisfied too.
     */
    public Map<Constraint, Exception> failedConstraints() {
      return failedConstraints;
    }

    public Optional<Principal> activeMembership() {
      return activeMembership;
    }

    /**
     * Check if access is allowed based on the ACL.
     */
    public boolean isSubjectInAcl() {
      return isSubjectInAcl;
    }

    /**
     * Check if access is allowed based on the ACL, constraints,
     * and active memberships.
     */
    public boolean isAllowed() {
      return this.activeMembership.isPresent() ||
        (this.isSubjectInAcl && this.unsatisfiedConstraints.isEmpty());
    }
  }
}
