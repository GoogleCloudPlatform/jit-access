//package com.google.solutions.jitaccess.catalog.analysis;
//
//
//import com.google.solutions.jitaccess.catalog.policy.Constraint;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Collection;
//
//public record AccessAnalysis(
//  boolean accessGranted,
//  boolean active,
//  @NotNull Collection<Constraint> satisfiedConstraints,
//  @NotNull Collection<Constraint> unsatisfiedConstraints
//  ) {
//
//  /**
//   * Check if access is allowed based on the analysis results.
//   */
//  public boolean isAllowed() {
//    return this.active ||
//        (this.accessGranted && this.unsatisfiedConstraints.isEmpty());
//  }
//}
