package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.solutions.jitaccess.apis.clients.AccessDeniedException;
import com.google.solutions.jitaccess.catalog.Catalog;
import com.google.solutions.jitaccess.catalog.JitGroup;
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
@Path("/api/catalog")
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

    return GroupInfo.fromJitGroup(this.catalog.group(groupId));
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

    var groups = this.catalog.groups(environment)
      .stream()
      .map(GroupInfo::fromJitGroup)
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
    @NotNull String cloudIdentityGroup,
    @NotNull SystemInfo system,
    @Nullable JoinAccessInfo access
  ) {
    static GroupInfo fromJitGroup(@NotNull JitGroup g) {
      var joinAccessInfo = g.analyzeJoinAccess()
        .map(a -> new JoinAccessInfo(a.isMembershipActive(),
          a.satisfiedConstraints().stream()
            .map(c -> new ConstraintInfo(c.name(), c.description()))
            .toList(),
          a.unsatisfiedConstraints().stream()
            .map(c -> new ConstraintInfo(c.name(), c.description()))
            .toList(),
          a.input().stream()
            .map(i -> new InputInfo(
              i.name(),
              i.displayName(),
              i.type().getSimpleName(),
              i.get(),
              i.minInclusive().orElse(null),
              i.maxInclusive().orElse(null)))
            .toList()))
        .orElse(null);

      //TODO: group email

      return new GroupInfo(
        g.group().id().toString(),
        g.group().name(),
        g.group().description(),
        g.cloudIdentityGroupId().email,
        new SystemInfo(
          g.group().system().name(),
          g.group().system().description()),
        joinAccessInfo);
    }
  }

  public record SystemInfo(
    @NotNull String id,
    @NotNull String name
  ) {}

  public record JoinAccessInfo(
    @NotNull boolean membershipActive,

    //TODO: expiry date
    //TODO: requiresApproval
    @NotNull List<ConstraintInfo> satisfiedConstraints,
    @NotNull List<ConstraintInfo> unsatisfiedConstraints,
    @NotNull List<InputInfo> input
  ) {}

  public record ConstraintInfo(
    @NotNull String name,
    @NotNull String description
  ) {}

  public record InputInfo(
    @NotNull String name,
    @NotNull String description,
    @NotNull String type,
    @NotNull String value,
    @Nullable String minInclusive,
    @Nullable String maxInclusive
  ) {}
}
