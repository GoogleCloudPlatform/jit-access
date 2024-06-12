package com.google.solutions.jitaccess.core.access;


import com.google.solutions.jitaccess.core.policy.Role;
import com.google.solutions.jitaccess.core.policy.constraints.Constraint;
import com.google.solutions.jitaccess.core.policy.constraints.Intent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record RoleAnalysis(
  @NotNull Intent intent,
  @NotNull Role role,
  boolean accessGranted,
  boolean active,
  @NotNull Collection<Constraint> satisfiedConstraints,
  @NotNull Collection<Constraint> unsatisfiedConstraints
  ) {

  public boolean canPerformIntent() {
    return this.active ||
        (this.accessGranted && this.unsatisfiedConstraints.isEmpty());
  }
}
