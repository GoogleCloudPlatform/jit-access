package com.google.solutions.jitaccess.core.access;


import com.google.solutions.jitaccess.core.policy.constraints.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record AccessAnalysis(
  @NotNull Request request,
  boolean accessGranted,
  boolean active,
  @NotNull Collection<Constraint> satisfiedConstraints,
  @NotNull Collection<Constraint> unsatisfiedConstraints
  ) {

  public boolean isGranted() {
    return this.active ||
        (this.accessGranted && this.unsatisfiedConstraints.isEmpty());
  }
}
