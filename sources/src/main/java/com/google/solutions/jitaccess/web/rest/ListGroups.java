package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.solutions.jitaccess.catalog.Catalog;
import jakarta.enterprise.context.Dependent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
      .map(g -> {
        var analysis = g.accessAnalysis();
        return new GroupInfo(
          g.group().id().toString(),
          g.group().name(),
          g.group().description(),
          analysis.isMembershipActive(),
          analysis.satisfiedConstraints().stream()
            .map(c -> new ConstraintInfo(c.name(), c.displayName()))
            .toList(),
          analysis.unsatisfiedConstraints().stream()
            .map(c -> new ConstraintInfo(c.name(), c.displayName()))
            .toList());
      })
      .collect(Collectors.toList());

    return new Response(groups);
  }

  public record Response(
    @NotNull List<GroupInfo> groups
  ) {
  }

  public record GroupInfo(
    @NotNull String id,
    @NotNull String name,
    @NotNull String description,
    @NotNull boolean membershipActive,
    @NotNull List<ConstraintInfo> satisfiedConstraints,
    @NotNull List<ConstraintInfo> unsatisfiedConstraints
  ) {}

  public record ConstraintInfo(
    @NotNull String name,
    @NotNull String displayName
  ) {}
}
