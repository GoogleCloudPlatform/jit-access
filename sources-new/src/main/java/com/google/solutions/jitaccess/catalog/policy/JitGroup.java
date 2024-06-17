package com.google.solutions.jitaccess.catalog.policy;

import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record JitGroup (//TODO: test
  @NotNull JitGroupId groupId,
  @NotNull String description,
  @NotNull AccessControlList acl,
  @NotNull List<Constraint> requestConstraints,
  @NotNull List<Constraint> approvalConstraints,
  @NotNull List<Constraint> validityConstraints
) {
}
