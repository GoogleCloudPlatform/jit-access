package com.google.solutions.jitaccess.catalog.policy;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record CatalogPolicy(
  @NotNull Map<String, EnvironmentPolicy> environments
  ) {

  public CatalogPolicy {
    Preconditions.checkArgument(
      environments.entrySet()
        .stream()
        .allMatch(e -> e.getKey().equals(e.getValue().name())),
      "Environment name must match key");
  }

  public Optional<EnvironmentPolicy> environment(String name) { // TODO: test
    var policy = this.environments.get(name);
    return policy != null
      ? Optional.of(policy)
      : Optional.empty();
  }
}
