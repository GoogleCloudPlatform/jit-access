package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.catalog.analysis.AccessAnalysis;
import com.google.solutions.jitaccess.catalog.policy.JitGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Catalog {
  // list requestable roles, analyze each
  @NotNull Collection<JoinableJitGroup> listJoinableGroups();

  // @NotNull Collection<UserAnalysis> listUsers(
  //   @NotNull RoleId role);
  //
  // @NotNull Request createRequest(
  //   @NotNull RoleId role),
  //   @NotNull Map<String, String> userInput,
  // Map<  Constraint -> user input>
  // );

  // createApproveRequest
}

record JoinableJitGroup(
  @NotNull JitGroup group,
  @NotNull AccessAnalysis access
  ) {}