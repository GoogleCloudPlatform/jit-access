package com.google.solutions.jitaccess.core.policy;

import com.google.solutions.jitaccess.core.access.Request;
import com.google.solutions.jitaccess.core.auth.JitGroupId;
import com.google.solutions.jitaccess.core.policy.constraints.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record JitGroup (
  @NotNull JitGroupId groupId,
  @NotNull String description,
  @NotNull Map<Class<? extends Request>, List<Constraint>> constraints,
  @NotNull AccessControlList acl
) {
}
