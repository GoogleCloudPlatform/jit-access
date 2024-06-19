package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.solutions.jitaccess.catalog.Catalog;
import jakarta.enterprise.context.Dependent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Dependent
public class ListGroups {//TODO: test
  private final @NotNull Catalog catalog;

  public ListGroups(@NotNull Catalog catalog) {
    this.catalog = catalog;
  }

  public @NotNull Response execute(@Nullable String environment) {
    Preconditions.checkArgument(
      !Strings.isNullOrEmpty(environment),
      "Environment must be specified");

    var groups = this.catalog.joinableGroups(environment)
      .stream()
      .map(group -> new Group(group.id().toString(), group.name(), group.description()))
      .collect(Collectors.toList());

    // TODO: access check details

    return new Response(groups);
  }

  public record Response(
    @NotNull List<Group> groups
  ) {
  }

  public record Group(
    @NotNull String id,
    @NotNull String name,
    @NotNull String description
  ) {}
}
