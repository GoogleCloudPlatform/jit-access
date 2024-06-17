package com.google.solutions.jitaccess.catalog.policy;

import com.google.solutions.jitaccess.catalog.analysis.AccessRequest;
import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record JitGroup (//TODO: test
  @NotNull JitGroupId groupId,
  @NotNull String description,
  @NotNull Map<Class<? extends AccessRequest>, List<Constraint>> constraints,
  @NotNull AccessControlList acl
) {
}
