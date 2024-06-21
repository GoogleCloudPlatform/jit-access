package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.solutions.jitaccess.apis.clients.AccessDeniedException;
import com.google.solutions.jitaccess.catalog.Catalog;
import com.google.solutions.jitaccess.catalog.auth.GroupId;
import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.web.RequireIapPrincipal;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@Dependent
@Path("/catalog")
@RequireIapPrincipal
public class GroupsResource {//TODO: test
  @Inject
  Catalog catalog;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments/{environment}/groups/{id}")
  public @NotNull GroupInfo getGroups(
    @PathParam("id") @Nullable String unparsedGroupId
  ) throws AccessDeniedException {
    var groupId = JitGroupId
      .parse(unparsedGroupId)
      .orElseThrow(() -> new IllegalArgumentException("The ID is invalid"));

    return GroupInfo.fromJoinableGroup(this.catalog.joinableGroup(groupId));
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments/{environment}/groups")
  public @NotNull GroupsInfo listGroups(
    @PathParam("environment") @Nullable String environment
  ) {
    Preconditions.checkArgument(
      !Strings.isNullOrEmpty(environment),
      "Environment must be specified");

    var groups = this.catalog.joinableGroups(environment)
      .stream()
      .map(GroupInfo::fromJoinableGroup)
      .collect(Collectors.toList());

    return new GroupsInfo(groups);
  }

  public record GroupsInfo(
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
  ) {
    static GroupInfo fromJoinableGroup(@NotNull Catalog.JoinableGroup g) {
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
    }

  }

  public record ConstraintInfo(
    @NotNull String name,
    @NotNull String displayName
  ) {}
}
