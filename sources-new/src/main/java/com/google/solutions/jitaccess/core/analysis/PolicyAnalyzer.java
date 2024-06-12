package com.google.solutions.jitaccess.core.analysis;

import com.google.common.collect.ImmutableList;
import com.google.solutions.jitaccess.core.auth.RoleId;
import com.google.solutions.jitaccess.core.policy.AccessRights;
import com.google.solutions.jitaccess.core.policy.constraints.Intent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PolicyAnalyzer {

  // - request constraints: conditions (on subject, request) that must be met to allow a request
  //     - 2sv, report, justification
  // - approval constraints: conditions (on subject, request) that must be met to approve a request
  // - validity constraints: conditions (on subject, role) that must be met for role to remain active
  //
  // some constraints are built-in, extensible using CEL


  /**
   *
   */
  public @NotNull Collection<RoleAnalysis> analyzeRoles(
    @NotNull Intent intent
  ) {
    // check access, constraints based on intent
    throw new RuntimeException();
  }

  /**
   *
   */
  public @NotNull RoleAnalysis analyzeRole(
    @NotNull RoleId role,
    @NotNull Intent intent
  ) {
    throw new RuntimeException();
  }
}
