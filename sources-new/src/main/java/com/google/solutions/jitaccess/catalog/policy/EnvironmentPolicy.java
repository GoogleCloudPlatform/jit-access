package com.google.solutions.jitaccess.catalog.policy;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Policy for an environment such as "prod".
 */
public record EnvironmentPolicy(
  @NotNull String name,
  @NotNull String description,
  @NotNull List<SystemPolicy> systems
) {
  /**
   * Maximum length for names, in characters.
   */
  static final int NAME_MAX_LENGTH = 16;

  public EnvironmentPolicy {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkArgument(
      name.length() <= NAME_MAX_LENGTH,
      String.format(
        "Environment names must not exceed %d characters in length",
        NAME_MAX_LENGTH));
  }

  public Optional<SystemPolicy> system(String name) { // TODO: test
    return this.systems
      .stream()
      .filter(s -> s.name().equals(name))
      .findFirst();
  }

  @Override
  public String toString() {
    return this.name;
  }
}
