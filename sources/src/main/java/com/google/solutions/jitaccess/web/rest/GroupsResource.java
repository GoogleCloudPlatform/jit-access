package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.solutions.jitaccess.apis.clients.AccessDeniedException;
import com.google.solutions.jitaccess.apis.clients.AccessException;
import com.google.solutions.jitaccess.catalog.Catalog;
import com.google.solutions.jitaccess.catalog.JitGroup;
import com.google.solutions.jitaccess.catalog.Logger;
import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.policy.PolicyAnalysis;
import com.google.solutions.jitaccess.catalog.policy.Property;
import com.google.solutions.jitaccess.web.RequireIapPrincipal;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Dependent
@Path("/api/catalog")
@RequireIapPrincipal
public class GroupsResource {//TODO: test
  @Inject
  Catalog catalog;

  @Inject
  Logger logger;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments/{environment}/groups/{id}")
  public @NotNull GroupInfo get(
    @PathParam("id") @Nullable String unvalidatedGroupId
  ) throws AccessDeniedException {
    var groupId = JitGroupId
      .parse(unvalidatedGroupId)
      .orElseThrow(() -> new IllegalArgumentException("The ID is invalid"));

    return GroupInfo.fromJitGroup(this.catalog.group(groupId));
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments/{environment}/groups")
  public @NotNull GroupsInfo list(
    @PathParam("environment") @Nullable String unvalidatedEnvironment
  ) {
    Preconditions.checkArgument(
      !Strings.isNullOrEmpty(unvalidatedEnvironment),
      "Environment must be specified");

    var groups = this.catalog
      .groups(unvalidatedEnvironment)
      .stream()
      .map(GroupInfo::fromJitGroup)
      .collect(Collectors.toList());

    return new GroupsInfo(groups);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments/{environment}/groups/{group}")
  public @NotNull GroupInfo join(
    @PathParam("environment") @Nullable String unvalidatedEnvironment,
    @PathParam("group") @Nullable String unvalidatedGroupId,
    MultivaluedMap<String, String> inputValues
  ) throws AccessException {
    Preconditions.checkArgument(
      !Strings.isNullOrEmpty(unvalidatedEnvironment),
      "Environment must be specified");
    Preconditions.checkArgument(
      !Strings.isNullOrEmpty(unvalidatedGroupId),
      "Group must be specified");

    var groupId = JitGroupId
      .parse(unvalidatedGroupId)
      .orElseThrow(() -> new IllegalArgumentException("The ID is invalid"));
    Preconditions.checkArgument(
      groupId.environment().equals(unvalidatedEnvironment),
      "The group is not part of this environment");

    var group = this.catalog.group(groupId);
    var joinOp = group.join();

    for (var input : joinOp.input()) {
      //
      // Set input. This might throw an exception if the
      // user-provided input it incomplete or invalid.
      //
      input.set(Stream
        .ofNullable(inputValues.get(input.name()))
        .flatMap(i -> i.stream())
        .findFirst()
        .orElse(null));
    }

    try {
      if (joinOp.requiresApproval()) {
        // TODO: create token, send out
      }
      else {
        var principal = joinOp.execute();

        return GroupInfo.fromJitGroup(
          group,
          new GroupAccessInfo(
            JoinStatusInfo.JOINED,
            new MembershipInfo(
              true,
              Optional.ofNullable(principal.expiry())
                .map(Instant::getEpochSecond)
                .get()),
            List.of(), // Don't repeat constraints
            List.of(), // Don't repeat constraints
            joinOp.input()
              .stream()
              .map(InputInfo::fromProperty)
              .toList()));
      }
    }
    catch (PolicyAnalysis.ConstraintFailedException e) {
      //
      // A failed constraint indicates a configuration issue, so
      // log all the details.
      //
      for (var detail : e.exceptions()) {
        this.logger.error(
          EventIds.API_CONSTRAINT_FAILURE,
          e.getMessage(),
          detail);
      }

      throw new AccessDeniedException(e.getMessage(), e);
    }

    //TODO: log, notify

    throw new RuntimeException("NIY");
  }

  public record GroupsInfo(
    @NotNull List<GroupInfo> groups
  ) implements ResponseEntity {
  }

  public record GroupInfo(
    @NotNull String id,
    @NotNull String name,
    @NotNull String description,
    @NotNull String cloudIdentityGroup,
    @NotNull SystemInfo system,
    @Nullable GroupAccessInfo access
  ) implements ResponseEntity {
    static GroupInfo fromJitGroup(@NotNull JitGroup g) {
      var joinOp = g.join();
      var analysis = joinOp.dryRun();
      return fromJitGroup(
        g,
        GroupAccessInfo.fromAnalysis(
        joinOp.requiresApproval()
          ? JoinStatusInfo.JOIN_ALLOWED_WITH_APPROVAL
          : JoinStatusInfo.JOIN_ALLOWED_WITHOUT_APPROVAL,
        analysis));
    }

    static GroupInfo fromJitGroup(
      @NotNull JitGroup g,
      @NotNull GroupsResource.GroupAccessInfo groupAccessInfo) {
      return new GroupInfo(
        g.group().id().toString(),
        g.group().name(),
        g.group().description(),
        g.cloudIdentityGroupId().email,
        new SystemInfo(
          g.group().system().name(),
          g.group().system().description()),
        groupAccessInfo);
    }
  }

  public record SystemInfo(
    @NotNull String id,
    @NotNull String name
  ) {}

  public record GroupAccessInfo( // TODO: JoinStatusInfo + status
     @NotNull JoinStatusInfo status,
     @NotNull MembershipInfo membership,
     @NotNull List<ConstraintInfo> satisfiedConstraints,
     @NotNull List<ConstraintInfo> unsatisfiedConstraints,
     @NotNull List<InputInfo> input
  ) {
    public static GroupAccessInfo fromAnalysis(
      @NotNull JoinStatusInfo status,
      @NotNull PolicyAnalysis.Result analysis
    ) {
      return new GroupAccessInfo(
        status,
        new MembershipInfo(
          analysis.activeMembership().isPresent(),
          analysis.activeMembership()
            .map(p -> p.expiry() != null ? p.expiry().getEpochSecond(): null)
            .orElse(null)),
        analysis.satisfiedConstraints().stream()
          .map(c -> new ConstraintInfo(c.name(), c.displayName()))
          .toList(),
        analysis.unsatisfiedConstraints().stream()
          .map(c -> new ConstraintInfo(c.name(), c.displayName()))
          .toList(),
        analysis.input().stream()
          .map(InputInfo::fromProperty)
          .toList());
    }
  }

  public enum JoinStatusInfo {
    JOIN_ALLOWED_WITHOUT_APPROVAL,
    JOIN_ALLOWED_WITH_APPROVAL,
    JOIN_REQUESTED,
    JOINED
  }

  public record MembershipInfo(
    boolean active,
    @Nullable Long expiry
  ) {}


  public record ConstraintInfo(
    @NotNull String name,
    @NotNull String description
  ) {}

  public record InputInfo(
    @NotNull String name,
    @NotNull String description,
    @NotNull String type,
    boolean isRequired,
    @NotNull String value,
    @Nullable String minInclusive,
    @Nullable String maxInclusive
  ) {
    static InputInfo fromProperty(@NotNull Property i) {
      return new InputInfo(
        i.name(),
        i.displayName(),
        i.type().getSimpleName(),
        i.isRequired(),
        i.get(),
        i.minInclusive().orElse(null),
        i.maxInclusive().orElse(null));
    }
  }
}
