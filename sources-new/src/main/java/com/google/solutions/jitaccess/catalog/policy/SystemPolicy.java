package com.google.solutions.jitaccess.catalog.policy;


import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Policy for a system.
 *
 * A "system" is a set of resources that are managed
 * jointly and form a logical unit. Examples include:
 *
 * - "Foo application backend"
 * - CI/CD system
 * - Data warehouse for the Bar app
 */
public record SystemPolicy(
  @NotNull EnvironmentPolicy parent,
  @NotNull String name,
  @NotNull String description,
  @NotNull List<JitGroupPolicy> groups
  ) {

  /**
   * Maximum length for names, in characters.
   */
  static final int NAME_MAX_LENGTH = 16;

  public SystemPolicy {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkArgument(
      name.length() <= NAME_MAX_LENGTH,
      String.format(
        "System names must not exceed %d characters in length",
        NAME_MAX_LENGTH));
  }

  public Optional<JitGroupPolicy> group(String name) { // TODO: test
    return this.groups
      .stream()
      .filter(s -> s.name().equals(name))
      .findFirst();
  }

  @Override
  public String toString() {
    return this.name;
  }
}
