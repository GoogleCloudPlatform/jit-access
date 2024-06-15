package com.google.solutions.jitaccess.catalog.analysis;


import com.google.solutions.jitaccess.catalog.policy.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record AccessAnalysis(
  @NotNull AccessRequest request,
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
