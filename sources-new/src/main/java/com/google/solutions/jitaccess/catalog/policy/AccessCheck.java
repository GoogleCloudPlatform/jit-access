package com.google.solutions.jitaccess.catalog.policy;

import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AccessCheck { // TODO: test
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
    this.subject = subject;
    this.policy = policy;
    this.groupId = groupId;
    this.requiredRights = requiredRights;
  }

  public @NotNull AccessCheck applyConstraints(
    @NotNull Collection<Constraint> constraints,
    @NotNull Map<String, Property> input
  ) {
    this.constraints = constraints;
    this.input = input;
    return this;
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
      resultAccumulator.satisfiedAcl &= policy.accessControlList()
        .get()
        .isAllowed(this.subject, PolicyRight.toMask(this.requiredRights));
    }

    if (this.constraints != null) {
      for (var constraint : this.constraints) {
        evaluateConstraint(constraint, resultAccumulator);
      }
    }
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
    result.active = this.subject
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
    private boolean satisfiedAcl;
    private boolean active;
    private @NotNull LinkedList<Constraint> satisfiedConstraints;
    private @NotNull LinkedList<Constraint> unsatisfiedConstraints;
    private @NotNull Map<Constraint, Exception> failedConstraints;

    public Result(boolean satisfiedAcl) {
      this.satisfiedAcl = satisfiedAcl;
      this.active = false;
      this.satisfiedConstraints = new LinkedList<>();
      this.unsatisfiedConstraints = new LinkedList<>();
      this.failedConstraints = new HashMap<>();
    }

    public boolean satisfiedAcl() {
      return satisfiedAcl;
    }

    public boolean active() {
      return active;
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
     * Check if access is allowed based on the analysis results.
     */
    public boolean isAllowed() {
      return this.active ||
        (this.satisfiedAcl && this.unsatisfiedConstraints.isEmpty());
    }
  }
}
