package com.google.solutions.jitaccess.core.service;

import com.google.solutions.jitaccess.core.access.AccessAnalysis;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Catalog {
  // list requestable roles, analyze each
  @NotNull Collection<AccessAnalysis> listAvailableRolesForSelf();

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
