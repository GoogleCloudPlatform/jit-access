package com.google.solutions.jitaccess.core.policy;

import com.google.solutions.jitaccess.core.auth.RoleId;
import com.google.solutions.jitaccess.core.policy.constraints.Constraint;
import com.google.solutions.jitaccess.core.policy.constraints.Intent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record Role(
  @NotNull RoleId roleId,
  @NotNull String description,
  @NotNull Map<Intent, List<Constraint>> constraints,
  @NotNull AccessControlList acl
) {
}
