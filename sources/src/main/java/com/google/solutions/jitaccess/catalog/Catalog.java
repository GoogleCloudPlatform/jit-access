package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.apis.clients.AccessDeniedException;
import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Catalog {//TODO: test
  private final @NotNull Map<String, EnvironmentPolicy> environments;
  private final @NotNull Subject subject;

  private Optional<EnvironmentPolicy> lookupEnvironmentWithoutAclCheck(@NotNull String name) {
    var env = this.environments.get(name);
    return env != null ? Optional.of(env) : Optional.empty();
  }

  private @NotNull Optional<JitGroupPolicy> lookupGroupWithoutAclCheck(@NotNull JitGroupId groupId) {
    return this.lookupEnvironmentWithoutAclCheck(groupId.environment())
      .flatMap(env -> env.system(groupId.system()))
      .flatMap(sys -> sys.group(groupId.name()));
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
    // NB. No access check required.
    //
    return this.environments.values();
  }

  /**
   * Get details for a group that the current subject could join.
   *
   * @return group details
   * @throws is group not found or access denied
   */
  public @NotNull JoinableGroup joinableGroup(
    @NotNull JitGroupId groupId
  ) throws AccessDeniedException {
    var group = lookupGroupWithoutAclCheck(groupId);
    if (!group.isPresent() || !group.get()
      .createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
      .execute()
      .isSubjectInAcl()) {
      throw new AccessDeniedException(
        String.format("The group '%s' does not exist or access is denied", groupId));
    }

    //
    // User in ACL, so we're ok to return this group. The
    // user might not satisfy all constraints though, which is ok.
    //
    return new JoinableGroup(this.subject, group.get());
  }

  /**
   * List groups that the current subject could join. Other groups
   * are filtered out.
   */
  public @NotNull Collection<JoinableGroup> joinableGroups(
    @NotNull String environmentName
  ) {
    var environment = this.lookupEnvironmentWithoutAclCheck(environmentName)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("The environment '%s' does not exist", environmentName)));

    var groups = new LinkedList<JoinableGroup>();
    for (var system : environment.systems()) {
      for (var group : system.groups()) {
        if (group
          .createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
          .execute()
          .isSubjectInAcl()) {
          //
          // User in ACL, so we're ok to return this group. The
          // user might not satisfy all constraints though, which is ok.
          //
          groups.add(new JoinableGroup(this.subject, group));
        }
      }
    }

    return groups;
  }

  /**
   * Group that a user could join.
   */
  public static class JoinableGroup {
    private final @NotNull Subject subject;
    private final @NotNull JitGroupPolicy group;

    private JoinableGroup(@NotNull Subject subject, @NotNull JitGroupPolicy group) {
      this.subject = subject;
      this.group = group;
    }

    /**
     * @return group details.
     */
    public @NotNull JitGroupPolicy group() {
      return this.group;
    }

    /**
     * @return details about possibly unmet constraints.
     */
    public @NotNull AccessCheck.Result accessAnalysis() {
      //
      // Perform full access check, incl. constraints.
      //
      return group
        .createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
        .applyConstraints(Policy.ConstraintClass.JOIN)
        .execute();
    }
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