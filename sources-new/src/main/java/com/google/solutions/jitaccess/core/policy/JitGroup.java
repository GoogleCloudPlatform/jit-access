package com.google.solutions.jitaccess.core.policy;

import com.google.solutions.jitaccess.core.auth.JitGroupId;
import com.google.solutions.jitaccess.core.policy.constraints.Constraint;
import com.google.solutions.jitaccess.core.policy.constraints.Intent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record JitGroup (
  @NotNull JitGroupId groupId,
  @NotNull String description,
  @NotNull Map<Intent, List<Constraint>> constraints,
  @NotNull AccessControlList acl
) {
}
