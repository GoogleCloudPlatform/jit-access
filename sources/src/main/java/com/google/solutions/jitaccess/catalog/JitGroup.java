package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.AccessCheck;
import com.google.solutions.jitaccess.catalog.policy.JitGroupPolicy;
import com.google.solutions.jitaccess.catalog.policy.Policy;
import com.google.solutions.jitaccess.catalog.policy.PolicyRight;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

/**
 * JIT Group in the context of a specific subject.
 */
public class JitGroup {
  private final @NotNull Subject subject;
  private final @NotNull JitGroupPolicy group;

  JitGroup(@NotNull Subject subject, @NotNull JitGroupPolicy group) {
    this.subject = subject;
    this.group = group;
  }

  /**
   * @return group details.
   */
  public @NotNull JitGroupPolicy group() {
    return this.group;
  }

  /**
   * @return details about possibly unmet constraints.
   */
  public @NotNull Optional<AccessCheck.Result> analyzeJoinAccess() {
    //
    // Analyze if the current subject can join this group, and what
    // constraints might be unsatisfied.
    //
    var result = group
      .createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
      .applyConstraints(Policy.ConstraintClass.JOIN)
      .execute();

    if (!result.isSubjectInAcl()) {
      //
      // Subject not in ACL, so we can't disclose any details.
      //
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }
}
