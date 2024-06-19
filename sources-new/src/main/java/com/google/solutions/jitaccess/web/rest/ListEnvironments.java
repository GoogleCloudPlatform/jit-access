package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.solutions.jitaccess.catalog.Catalog;
import jakarta.enterprise.context.Dependent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Dependent
public class ListEnvironments {//TODO: test
  private final @NotNull Catalog catalog;

  public ListEnvironments(@NotNull Catalog catalog) {
    this.catalog = catalog;
  }

  public @NotNull Response execute() {
    var environments = this.catalog.environments()
      .stream()
      .map(env -> new EnvironmentInfo(env.name(), env.description()))
      .collect(Collectors.toList());

    return new Response(environments);
  }

  public record Response(
    @NotNull List<EnvironmentInfo> environments
  ) {
  }

  public record EnvironmentInfo(
    @NotNull String name,
    @NotNull String description
  ) {}
}
