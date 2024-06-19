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
import com.google.solutions.jitaccess.catalog.auth.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.*;

public class AccessCheck {
  private final @NotNull Subject subject;
  private final @NotNull JitGroupId groupId;
  private final @NotNull Policy policy;
  private final @NotNull EnumSet<PolicyRight> requiredRights;

  private @Nullable Collection<Constraint> constraints;
  private @Nullable Map<String, Property> input;

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

  private void evaluateConstraint(
    @NotNull Constraint c,
    @NotNull Result resultAccumulator
  ) {
    if (!c.expectedInput().keySet().stream().allMatch(k -> this.input.containsKey(k))) {
      resultAccumulator.unsatisfiedConstraints.add(c);
      resultAccumulator.failedConstraints.put(
        c,
        new IllegalArgumentException("One or more required input properties are missing"));

      return;
    }

    var check = c.createCheck();

    //
    // Copy input properties.
    //
    var input = check.addContext("input");
    for (var entry : this.input.entrySet()) {
      input.set(entry.getKey(), entry.getValue().value());
    }

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
        resultAccumulator.satisfiedConstraints.add(c);
      }
      else {
        resultAccumulator.unsatisfiedConstraints.add(c);
      }
    } catch (ConstraintException e) {
      resultAccumulator.unsatisfiedConstraints.add(c);
      resultAccumulator.failedConstraints.put(c, e);
    }
  }

  @TestOnly
  Result evaluateConstraint(@NotNull Constraint c) {
    var result = new Result(false);
    evaluateConstraint(c, result);
    return result;
  }

  private void evaluateAclAndConstraints(
    @NotNull Policy policy,
    @NotNull Result resultAccumulator
  ) {
    //
    // Evaluate the environment policy first.
    //
    if (policy.parent().isPresent()) {
      evaluateAclAndConstraints(policy.parent().get(), resultAccumulator);
    }

    //
    // Check ACL. If any ACL in the hierarchy denies access,
    // we deny overall access. Therefore, we AND-combine
    // the individual results.
    //
    if (policy.accessControlList().isPresent())
    {
      resultAccumulator.isSubjectInAcl &= policy.accessControlList()
        .get()
        .isAllowed(this.subject, PolicyRight.toMask(this.requiredRights));
    }

    if (this.constraints != null) {
      for (var constraint : this.constraints) {
        evaluateConstraint(constraint, resultAccumulator);
      }
    }
  }

  public @NotNull AccessCheck applyConstraints(
    @NotNull Collection<Constraint> constraints,
    @NotNull Map<String, Property> input
  ) {
    this.constraints = constraints;
    this.input = input;
    return this;
  }

  public Result execute() {
    //
    // Evaluate ACL and constraints of the policy hierarchy.
    //
    var result = new Result(true);
    evaluateAclAndConstraints(this.policy, result);

    //
    // Check if the current user has the principal, i.e.,
    // has joined this group before. We only need to do this
    // once as it doesn't depend on the policy.
    //
    result.isMembershipActive = this.subject
      .principals()
      .stream()
      .filter(p -> p.isValid())
      .anyMatch(p -> p.id().equals(this.groupId));

    assert result.failedConstraints
      .keySet()
      .stream()
      .allMatch(c -> result.unsatisfiedConstraints.contains(c));

    return result;
  }

  public class Result {
    private boolean isSubjectInAcl;
    private boolean isMembershipActive;
    private @NotNull LinkedList<Constraint> satisfiedConstraints;
    private @NotNull LinkedList<Constraint> unsatisfiedConstraints;
    private @NotNull Map<Constraint, Exception> failedConstraints;

    private Result(boolean isSubjectInAcl) {
      this.isSubjectInAcl = isSubjectInAcl;
      this.isMembershipActive = false;
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
     * @return failed constraints and the exception they encountered.
     *
     * Failed constraints are always unsatisfied too.
     */
    public Map<Constraint, Exception> failedConstraints() {
      return failedConstraints;
    }

    /**
     * Check if the subject has an active membership.
     */
    public boolean isMembershipActive() {
      return isMembershipActive;
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
      return this.isMembershipActive ||
        (this.isSubjectInAcl && this.unsatisfiedConstraints.isEmpty());
    }
  }
}
