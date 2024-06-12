package com.google.solutions.jitaccess.core.service;

import com.google.solutions.jitaccess.core.access.AccessAnalyzer;
import com.google.solutions.jitaccess.core.access.RoleAnalysis;
import com.google.solutions.jitaccess.core.auth.RoleId;
import com.google.solutions.jitaccess.core.policy.constraints.Intent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public interface Catalog {
  // list requestable roles, analyze each
  @NotNull Collection<RoleAnalysis> listAvailableRolesForSelf();

  // @NotNull Collection<UserAnalysis> listUsers(
  //   @NotNull RoleId role);
  //
  // @NotNull Request createRequest(
  //   @NotNull RoleId role),
  //   @NotNull Map<String, String> userInput,
  // Map<  Constraint -> user input>
  // );

  // approve(Request)
}
