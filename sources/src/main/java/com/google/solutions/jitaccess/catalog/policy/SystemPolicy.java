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
import org.antlr.v4.runtime.tree.Tree;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Policy for a system.
 *
 * A "system" is a set of resources that are managed
 * jointly and form a logical unit. Examples include:
 *
 * - "Foo application backend"
 * - CI/CD system
 * - Data warehouse for the Bar app
 */
public record SystemPolicy(
  @NotNull EnvironmentPolicy environment,
  @NotNull String name,
  @NotNull String description,
  @NotNull List<JitGroupPolicy> groups
  ) implements Policy {

  /**
   * Maximum length for names, in characters.
   */
  static final int NAME_MAX_LENGTH = 16;
  static final String NAME_PATTERN = "[a-zA-Z0-9\\-]+";

  public SystemPolicy {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkArgument(
      name.length() <= NAME_MAX_LENGTH,
      String.format(
        "System names must not exceed %d characters in length",
        NAME_MAX_LENGTH));
    Preconditions.checkArgument(
      name.matches(NAME_PATTERN),
      "System names must only contain letters, numbers, and hyphens");

    environment.systems().add(this);
  }

  public SystemPolicy(
    @NotNull EnvironmentPolicy parent,
    @NotNull String name,
    @NotNull String description
  ) {
    this(parent, name, description, new LinkedList<>());
  }

  public Optional<JitGroupPolicy> group(@NotNull String name) {
    return this.groups
      .stream()
      .filter(s -> s.name().equals(name))
      .findFirst();
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public Optional<Policy> parent() {
    return Optional.of(this.environment);
  }

  @Override
  public Optional<AccessControlList> accessControlList() {
    return Optional.empty();
  }

  @Override
  public @NotNull Collection<Constraint> constraints(ConstraintClass c) {
    return List.of();
  }
}
