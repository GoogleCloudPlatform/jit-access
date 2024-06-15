package com.google.solutions.jitaccess.core.access;

import com.google.solutions.jitaccess.core.auth.JitGroupId;
import com.google.solutions.jitaccess.core.auth.Subject;
import com.google.solutions.jitaccess.core.policy.constraints.Intent;
import org.jetbrains.annotations.NotNull;

public class AccessAnalyzer {

  // - request constraints: conditions (on subject, request) that must be met to allow a request
  //     - 2sv, report, justification
  // - approval constraints: conditions (on subject, request) that must be met to approve a request
  // - validity constraints: conditions (on subject, role) that must be met for role to remain active
  //
  // some constraints are built-in, extensible using CEL


  // redundant
  // public @NotNull Collection<RoleAnalysis> analyzeRoles(
  //   @NotNull Intent intent
  // ) {
  //   // check access, constraints based on intent
  //   throw new RuntimeException();
  // }

  /**
   * 
   */
  public @NotNull AccessAnalysis analyze(
    @NotNull Subject subject,
    @NotNull JitGroupId role,
    @NotNull Intent intent
  ) {
    throw new RuntimeException();
  }

  // list users?!
}
