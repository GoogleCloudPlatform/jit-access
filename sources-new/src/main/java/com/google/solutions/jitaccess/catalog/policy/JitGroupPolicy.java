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

package com.google.solutions.jitaccess.catalog.policy;

import com.google.common.base.Preconditions;
import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Policy for a JIT group.
 *
 * @param description description for the group
 * @param acl access control list
 */
public record JitGroupPolicy( // TODO: Drop suffix
  @NotNull SystemPolicy system,
  @NotNull String name,
  @NotNull String description,
  @NotNull AccessControlList acl,
  @NotNull Map<ConstraintClass, Collection<Constraint>> constraints
) implements Policy {
  /**
   * Maximum length for names, in characters.
   */
  static final int NAME_MAX_LENGTH = 24;
  static final String NAME_PATTERN = "[a-zA-Z0-9\\-]+";

  public JitGroupPolicy {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkArgument(
      name.length() <= NAME_MAX_LENGTH,
      String.format(
        "JIT group names must not exceed %d characters in length",
        NAME_MAX_LENGTH));
    Preconditions.checkArgument(
      name.matches(NAME_PATTERN),
      "JIT group names must only contain letters, numbers, and hyphens");

    system.groups().add(this);
  }

  public JitGroupId id() {
    return new JitGroupId(
      this.system.environment().name(),
      this.system.name(),
      this.name);
  }

  public @NotNull AccessCheck createAccessCheck(
    @NotNull Subject subject,
    @NotNull EnumSet<PolicyRight> requiredRights
  ) {
    return new AccessCheck(
      this,
      subject,
      id(),
      requiredRights);
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public @NotNull Optional<Policy> parent() {
    return Optional.of(this.system);
  }

  @Override
  public @NotNull Optional<AccessControlList> accessControlList() {
    return Optional.of(this.acl);
  }

  @Override
  public @NotNull Collection<Constraint> constraints(ConstraintClass c) { // TODO: test
    var constraints = this.constraints.get(c);
    return constraints != null
      ? constraints
      : List.of();
  }
}
