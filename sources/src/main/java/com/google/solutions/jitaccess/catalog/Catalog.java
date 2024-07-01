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

package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.apis.clients.AccessDeniedException;
import com.google.solutions.jitaccess.catalog.auth.GroupMapping;
import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.EnvironmentPolicy;
import com.google.solutions.jitaccess.catalog.policy.PolicyAccess;
import com.google.solutions.jitaccess.catalog.policy.PolicyAnalysis;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Catalog of groups that a subject can access.
 *
 * This class serves as the "entry point" for the API/UI to
 * lookup or join groups.
 */
public class Catalog {
  private final @NotNull Map<String, EnvironmentPolicy> environments;
  private final @NotNull Subject subject;
  private final @NotNull GroupMapping groupMapping;

  Subject subject() {
    return subject;
  }

  GroupMapping groupMapping() {
    return groupMapping;
  }

  public Catalog(
    @NotNull Subject subject,
    @NotNull Map<String, EnvironmentPolicy> environments,
    @NotNull GroupMapping groupMapping
  ) {
    this.subject = subject;
    this.environments = environments;
    this.groupMapping = groupMapping;
  }

  public @NotNull Optional<EnvironmentPolicy> environment(@NotNull String name) {
    //
    // NB. No access check required.
    //
    var env = this.environments.get(name);
    return env != null ? Optional.of(env) : Optional.empty();
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
    var group = this.environment(groupId.environment())
      .flatMap(env -> env.system(groupId.system()))
      .flatMap(sys -> sys.group(groupId.name()));

    if (!group.isPresent() || !group.get()
      .analyze(this.subject, EnumSet.of(PolicyAccess.VIEW))
      .execute()
      .isAccessAllowed(PolicyAnalysis.AccessOptions.NONE)) {
      throw new AccessDeniedException(
        String.format("The group '%s' does not exist or access is denied", groupId));
    }

    //
    // User is allowed to view the group, so we're ok to return
    // the details.
    //
    // The user may our may not be allowed to join the group.
    //
    return new JitGroup(this, group.get());
  }

  /**
   * List JIT groups that the current subject can view. Non-JIT groups
   * are filtered out.
   */
  public @NotNull Collection<JitGroup> groups(
    @NotNull String environmentName
  ) {
    var environment = this.environment(environmentName)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("The environment '%s' does not exist", environmentName)));

    var groups = new LinkedList<JitGroup>();
    for (var system : environment.systems()) {
      for (var group : system.groups()) {
        if (group
          .analyze(this.subject, EnumSet.of(PolicyAccess.VIEW))
          .execute()
          .isAccessAllowed(PolicyAnalysis.AccessOptions.NONE)) {
          //
          // User in ACL, so we're ok to return this group. The
          // user might not satisfy all constraints though, which is ok.
          //
          groups.add(new JitGroup(this, group));
        }
      }
    }

    //
    // Sort the results by ID.
    //
    return groups.stream()
      .sorted(Comparator.comparing(g -> g.group().id()))
      .toList();
  }
}
