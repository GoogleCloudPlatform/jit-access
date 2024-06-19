package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.AccessCheck;
import com.google.solutions.jitaccess.catalog.policy.CatalogPolicy;
import com.google.solutions.jitaccess.catalog.policy.JitGroupPolicy;
import com.google.solutions.jitaccess.catalog.policy.PolicyRight;
import jakarta.enterprise.context.Dependent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Map;

@Dependent
public class Catalog {
  private final @NotNull CatalogPolicy policy;
  private final @NotNull Subject subject;

  public Catalog(@NotNull CatalogPolicy policy, @NotNull Subject subject) {
    this.policy = policy;
    this.subject = subject;
  }

  // list requestable roles, analyze each
  public @NotNull Collection<JoinableJitGroup> listJoinableGroups(//TODO: paging
    @NotNull String environmentName
  ) {
    var environment = this.policy.environment(environmentName)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("The environment '%s' does not exist", environmentName)));

    var joinables = new LinkedList<JoinableJitGroup>();
    for (var system : environment.systems()) {
      for (var group : system.groups()) {

        //
        // Perform an access check to see what constraints
        // the current subject would or wouldn't meet.
        //
        var access = group.createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
          .applyConstraints(
            group.constraints(JitGroupPolicy.LifecycleAction.JOIN),
            Map.of())
          .execute();

        if (access.isSubjectInAcl()) {
          //
          // At least one ACL check failed, so we can't even
          // surface this group.
          //
        }
        else {
          joinables.add(new JoinableJitGroup(group, access));
        }
      }
    }

    return joinables;
  }
//
//   public @NotNull Collection<AccessAnalysis> listUsers(@NotNull JitGroupId groupId) {
//     var groupPolicy = this.policy
//       .environment(groupId.environment())
//       .flatMap(env -> env.system(groupId.system()))
//       .flatMap(sys -> sys.group(groupId.name()))
//       .orElseThrow(() -> new IllegalArgumentException(
//         String.format("The JIT group '%s' does not exist", groupId)));
//
//     //TODO: groupPolicy.acl().isAllowed()
//     //TODO: groupPolicy.acl().allowedPrincipals(AccessRights.APPROVE_OTHERS)
//
//    throw new RuntimeException("NIY");
//   }
  //
  // @NotNull Request createJoinRequest(
  //   @NotNull RoleId role),
  //   @NotNull Map<String, String> userInput,
  // Map<  Constraint -> user input>
  // );

  // createApproveRequest
}

record JoinableJitGroup(
  @NotNull JitGroupPolicy group,
  @NotNull AccessCheck.Result access
  ) {}