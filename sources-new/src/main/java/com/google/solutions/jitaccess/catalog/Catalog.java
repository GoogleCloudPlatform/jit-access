package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.catalog.policy.AccessCheck;
import com.google.solutions.jitaccess.catalog.policy.JitGroupPolicy;
import org.jetbrains.annotations.NotNull;

public class Catalog {
//  private final CatalogPolicy policy;
//
//
//  // list requestable roles, analyze each
//  public @NotNull Collection<JoinableJitGroup> listJoinableGroups(
//    @NotNull String environmentName
//  ) {
//    var environment = this.policy.environment(environmentName)
//      .orElseThrow(() -> new IllegalArgumentException(
//        String.format("The environment '%s' does not exist", environmentName)));
//
//    var joinables = new LinkedList<JoinableJitGroup>();
//    for (var system : environment.systems()) {
//      for (var group : system.groups()) {
//        //
//        // Perform a basic access check to verify that we're ok
//        // to surface this group.
//        //
//        //TODO: groupPolicy.acl().isAllowed()
//
//        //
//        // Perform a deeper access check to see what constraints
//        // the current subject would or wouldn't meet.
//        //
//        AccessAnalysis analysis = null; //TODO: create request, analyze
//        joinables.add(new JoinableJitGroup(group, analysis));
//      }
//    }
//
//    return joinables;
//  }
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