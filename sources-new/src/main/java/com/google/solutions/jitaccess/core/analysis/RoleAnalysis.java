package com.google.solutions.jitaccess.core.analysis;


import com.google.solutions.jitaccess.core.policy.Role;
import com.google.solutions.jitaccess.core.policy.constraints.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record RoleAnalysis(
  @NotNull Role role,
  boolean accessGranted,
  boolean active,
  @NotNull Collection<Constraint> satisfiedConstraints,
  @NotNull Collection<Constraint> unsatisfiedConstraints
  ) {

  public boolean isSuccessful() {
    return this.active ||
        (this.accessGranted && this.unsatisfiedConstraints.isEmpty());
  }
}
