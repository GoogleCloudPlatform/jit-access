//
// Copyright 2024 Google LLC
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.google.solutions.jitaccess.auth;

import com.google.api.services.cloudidentity.v1.model.Membership;
import com.google.api.services.cloudidentity.v1.model.MembershipRelation;
import com.google.solutions.jitaccess.apis.Logger;
import com.google.solutions.jitaccess.apis.clients.AccessDeniedException;
import com.google.solutions.jitaccess.apis.clients.AccessException;
import com.google.solutions.jitaccess.apis.clients.CloudIdentityGroupsClient;
import com.google.solutions.jitaccess.apis.clients.ResourceNotFoundException;
import com.google.solutions.jitaccess.catalog.EventIds;
import com.google.solutions.jitaccess.common.CompletableFutures;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Resolves information necessary to build a subject from a user ID.
 */
public class SubjectResolver {
  private final @NotNull CloudIdentityGroupsClient groupsClient;
  private final @NotNull GroupMapping groupMapping;
  private final @NotNull Directory internalDirectory;
  private final @NotNull Executor executor;
  private final @NotNull Logger logger;

  public SubjectResolver(
    @NotNull CloudIdentityGroupsClient groupsClient,
    @NotNull GroupMapping groupMapping,
    @NotNull Directory internalDirectory,
    @NotNull Executor executor,
    @NotNull Logger logger
  ) {
    this.groupsClient = groupsClient;
    this.groupMapping = groupMapping;
    this.internalDirectory = internalDirectory;
    this.executor = executor;
    this.logger = logger;
  }

  @NotNull Set<Principal> resolveJitGroupMemberships(
    @NotNull EndUserId user,
    @NotNull List<UnresolvedMembership> memberships
  ) {
    assert memberships
      .stream()
      .allMatch(m -> this.groupMapping.isJitGroup(m.group));

    //
    // Lookup details for each membership.
    //
    var resolvedMembershipsFuture = CompletableFutures.mapAsync(
      memberships,
      membership -> {
        try {
          return Optional.of(new ResolvedMembership(
            membership.group,
            this.groupsClient.getMembership(membership.membershipId)));
        }
        catch (ResourceNotFoundException e) {
          //
          // Membership has been removed (or has expired) in the meantime,
          // we can ignore that.
          //
          return Optional.<ResolvedMembership>empty();
        }
      },
      this.executor);

    var principals = new HashSet<Principal>();
    try {
      for (var membership : resolvedMembershipsFuture.get()
        .stream()
        .flatMap(Optional::stream)
        .toList()) {

        assert membership.details
          .getPreferredMemberKey()
          .getId()
          .equals(user.email);

        //
        // NB. Temporary group memberships don't have a start date, but they
        // must have an expiry date.
        //
        var expiryDate = membership.details.getRoles()
          .stream()
          .filter(r -> r.getExpiryDetail() != null && r.getExpiryDetail().getExpireTime() != null)
          .map(d -> Instant.parse(d.getExpiryDetail().getExpireTime()))
          .min(Instant::compareTo)
          .orElse(null);

        if (expiryDate == null) {
          //
          // This is not a proper JIT group. Somebody might have created a group
          // that just happens to fit the naming convention.
          //
          this.logger.warn(
            EventIds.SUBJECT_RESOLUTION,
            String.format(
              "The group '%s' looks like a JIT group, but lacks an expiry date",
              membership.group()));
        }
        else {
          principals.add(new Principal(
            this.groupMapping.jitGroupFromGroup(membership.group()),
            expiryDate));
        }
      }
    }
    catch (InterruptedException | ExecutionException e) {
      this.logger.error(
        EventIds.SUBJECT_RESOLUTION,
        String.format(
          "Resolving JIT group memberships for user '%s' failed", user),
        e);
    }

    return principals;
  }

  /**
   * Create principals for all of a user's group memberships.
   */
  protected @NotNull Set<Principal> resolveGroupPrincipals(
    @NotNull EndUserId user
  ) throws AccessException, IOException {
    //
    // Find the user's direct group memberships. This includes all
    // groups, JIT role groups and others.
    //
    List<MembershipRelation> allMemberships;
    try {
      allMemberships = this.groupsClient
        .listMembershipsByUser(user)
        .stream()
        .toList();
    }
    catch (ResourceNotFoundException e) {
      //
      // This user doesn't exist. This shouldn't happen outside development mode
      // as IAP pre-authenticates all users.
      //
      // NB. If the user exists, but not in this Cloud Identity account, we should
      // get an empty response, not a 404.
      //
      throw new AccessDeniedException(
        "Resolving group membership failed because the user does not exist");
    }

    //
    // Separate memberships into two buckets:
    // - JIT groups: these need further processing
    // - other groups: can be used as-is
    //
    var otherGroupPrincipals = allMemberships
      .stream()
      .filter(m -> !this.groupMapping.isJitGroup(new GroupId(m.getGroupKey().getId())))
      .map(m -> new Principal(new GroupId(m.getGroupKey().getId())))
      .collect(Collectors.toSet());

    var jitGroupMemberships = allMemberships
      .stream()
      .filter(m -> this.groupMapping.isJitGroup(new GroupId(m.getGroupKey().getId())))
      .map(m -> new UnresolvedMembership(
        new GroupId(m.getGroupKey().getId()),
        new CloudIdentityGroupsClient.MembershipId(m.getMembership())))
      .toList();

    assert otherGroupPrincipals.size() + jitGroupMemberships.size() == allMemberships.size();
    assert otherGroupPrincipals.stream().allMatch(g -> g.id().value().contains("@"));

    //
    // For JIT groups, we need to know the expiry. The API doesn't
    // return that, so we have to perform extra lookups.
    //
    // NB. Other groups might have an expiry too. That expiry would be
    //     relevant if we were to cache the data. But we're not doing that,
    //     so we don't need to worry about it.
    //
    assert allMemberships
      .stream()
      .filter(m -> this.groupMapping.isJitGroup(new GroupId(m.getGroupKey().getId())))
      .filter(m -> m.getRoles() != null)
      .flatMap(m -> m.getRoles().stream())
      .allMatch(r -> r.getExpiryDetail() == null);

    var jitGroupPrincipals = resolveJitGroupMemberships(user, jitGroupMemberships);

    this.logger.info(
      EventIds.SUBJECT_RESOLUTION,
      String.format("The user '%s' is a member of %d JIT groups and %d other groups",
        user,
        jitGroupPrincipals.size(),
        otherGroupPrincipals.size()));

    var allGroupPrincipals = new HashSet<Principal>();
    allGroupPrincipals.addAll(otherGroupPrincipals);
    allGroupPrincipals.addAll(jitGroupPrincipals);
    return allGroupPrincipals;
  }

  /**
   * Lookup all of a user's principals. These include:
   *
   * <ul>
   *   <li>The user itself</li>
   *   <li>JIT Group memberships</li>
   *   <li>Other group memberships</li>
   * </ul>
   */
  public @NotNull Set<Principal> resolvePrincipals(
    @NotNull EndUserId user,
    @NotNull Directory directory
  ) throws AccessException, IOException {

    var allPrincipals = new HashSet<Principal>();
    allPrincipals.add(new Principal(ClassPrincipalSet.IAP_USERS));
    allPrincipals.add(new Principal(user));
    allPrincipals.addAll(resolveGroupPrincipals(user));

    //
    // Add an extra principal based on the directory the user
    // belongs to. This principal can be used for deny-purposes,
    // for example to deny external users from viewing or joining
    // a group.
    //
    if (directory.type() == Directory.Type.CLOUD_IDENTITY &&
      directory.equals(this.internalDirectory)) {
      //
      // This user belongs to the internal directory.
      //
      allPrincipals.add(new Principal(ClassPrincipalSet.INTERNAL_USERS));
    }
    else {
      //
      // This user does not belong to the internal directory.
      //
      allPrincipals.add(new Principal(ClassPrincipalSet.EXTERNAL_USERS));
    }

    //
    // Add a domain:DOMAIN principal if this is a managed user.
    //
    if (directory.type() == Directory.Type.CLOUD_IDENTITY) {
      allPrincipals.add(new Principal(new CloudIdentityDirectoryPrincipalSet(directory)));
    }

    return allPrincipals;
  }

  record UnresolvedMembership(
    @NotNull GroupId group,
    @NotNull CloudIdentityGroupsClient.MembershipId membershipId
  ) {}

  private record ResolvedMembership(
    @NotNull GroupId group,
    @NotNull Membership details
  ) {}
}
