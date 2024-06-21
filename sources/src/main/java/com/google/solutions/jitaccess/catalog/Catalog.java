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
   * Get details for a JIT group.
   *
   * @return group details
   * @throws is group not found or access denied
   */
  public @NotNull JitGroup group(
    @NotNull JitGroupId groupId
  ) throws AccessDeniedException {
    var group = lookupGroupWithoutAclCheck(groupId);
    if (!group.isPresent() || !group.get()
      .createAccessCheck(this.subject, EnumSet.of(PolicyRight.VIEW))
      .execute()
      .isSubjectInAcl()) {
      throw new AccessDeniedException(
        String.format("The group '%s' does not exist or access is denied", groupId));
    }

    //
    // User is allowed to view the group, so we're ok to return
    // the details.
    //
    // The user may our may not be allowed to join the group.
    //
    return new JitGroup(this.subject, group.get());
  }

  /**
   * List JIT groups that the current subject can view. Non-JIT groups
   * are filtered out.
   */
  public @NotNull Collection<JitGroup> groups(
    @NotNull String environmentName
  ) {
    var environment = this.lookupEnvironmentWithoutAclCheck(environmentName)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("The environment '%s' does not exist", environmentName)));

    var groups = new LinkedList<JitGroup>();
    for (var system : environment.systems()) {
      for (var group : system.groups()) {
        if (group
          .createAccessCheck(this.subject, EnumSet.of(PolicyRight.VIEW))
          .execute()
          .isSubjectInAcl()) {
          //
          // User in ACL, so we're ok to return this group. The
          // user might not satisfy all constraints though, which is ok.
          //
          groups.add(new JitGroup(this.subject, group));
        }
      }
    }

    return groups;
  }

  /**
   * Group that a user could join.
   */
  public static class JitGroup {
    private final @NotNull Subject subject;
    private final @NotNull JitGroupPolicy group;

    private JitGroup(@NotNull Subject subject, @NotNull JitGroupPolicy group) {
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
    public @NotNull Optional<AccessCheck.Result> analyzeJoinAccess() {
      //
      // Analyze if the current subject can join this group, and what
      // constraints might be unsatisfied.
      //
      var result = group
        .createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
        .applyConstraints(Policy.ConstraintClass.JOIN)
        .execute();

      if (!result.isSubjectInAcl()) {
        //
        // Subject not in ACL, so we can't disclose any details.
        //
        return Optional.empty();
      }
      else {
        return Optional.of(result);
      }
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