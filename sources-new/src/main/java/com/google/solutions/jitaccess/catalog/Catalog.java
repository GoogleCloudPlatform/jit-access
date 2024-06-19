package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.*;
import jakarta.enterprise.context.Dependent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Catalog {
  private final @NotNull Map<String, EnvironmentPolicy> environments;
  private final @NotNull Subject subject;

  private Optional<EnvironmentPolicy> environment(@NotNull String name) {
    var env = this.environments.get(name);
    return env != null ? Optional.of(env) : Optional.empty();
  }

  private @NotNull JitGroupPolicy lookupGroupWithouAclCheck(@NotNull JitGroupId groupId) {
    return this.environment(groupId.environment())
      .flatMap(env -> env.system(groupId.system()))
      .flatMap(sys -> sys.group(groupId.name()))
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("The group '%s' does not exist", groupId)));
  }

  public Catalog(
    @NotNull Subject subject,
    @NotNull Map<String, EnvironmentPolicy> environments
  ) {
    this.subject = subject;
    this.environments = environments;
  }

  /**
   * @return list of environments.
   */
  public @NotNull Collection<EnvironmentPolicy> environments() {
    //
    // NB. No access check requires.
    //
    return this.environments.values();
  }

  /**
   * @return groups in the environment that the subject might be
   * allowed to join
   */
  public @NotNull SortedSet<JitGroupPolicy> joinableGroups(
    @NotNull String environmentName
  ) {
    var environment = this.environment(environmentName)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("The environment '%s' does not exist", environmentName)));

    var groups = new TreeSet<JitGroupPolicy>();
    for (var system : environment.systems()) {
      for (var group : system.groups()) {
        //
        // Check ACL to ensure that we'e ok to return the group.
        // At this point, it doesn't matter if any of the constraints
        // isn't satisfied.
        //
        if (group
          .createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
          .execute()
          .isSubjectInAcl()) {
          groups.add(group);
        }
      }
    }

    return groups;
  }
//
//  @NotNull JoinOperation join(@NotNull JitGroupId groupId) {
//    var group = getGroupWithoutAclCheck(groupId);
//    group
//      .createAccessCheck(
//        this.subject,
//        EnumSet.of(PolicyRight.JOIN, PolicyRight.APPROVE_SELF))
//      .applyConstraints(Policy.ConstraintClass.JOIN);
//  }
//
//  @NotNull RequestJoinOperation requestJoin(@NotNull JitGroupId groupId) {
//
//  }
//
//  @NotNull ApproveJoinOperation approveJoin(@NotNull DelegationToken token) {
//
//  }
}

//abstract class CatalogOperation<T>  {
//  private final @NotNull JitGroupId groupId;
//  private final @NotNull AccessCheck accessCheck;
//
//  public @NotNull List<Property> input() {
//    return this.accessCheck.input();
//  }
//
//  protected abstract T executeCore();
//
//  public final T execute() {
//    var accessResult = this.accessCheck.execute();
//    if (!accessResult.isAllowed()) {
//      // TODO: handle error...
//    }
//
//    return executeCore();
//  }
//}
//
//class JoinOperation extends CatalogOperation<Principal> {
//
//}
//
//class RequestJoinOperation extends CatalogOperation<DelegationToken> {
//  // toToken
//}
//
//class ApproveJoinOperation extends CatalogOperation<Principal> {
//}
//
//class DelegationToken {}