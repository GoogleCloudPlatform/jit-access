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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class SystemPolicy extends AbstractPolicy {
  /**
   * Maximum length for names, in characters.
   */
  static final int NAME_MAX_LENGTH = 16;
  static final String NAME_PATTERN = "[a-zA-Z0-9\\-]+";

  private final @NotNull Map<String, JitGroupPolicy> groups = new TreeMap<>();

  public SystemPolicy(
    @NotNull String name,
    @NotNull String description,
    @Nullable AccessControlList acl,
    @NotNull Map<ConstraintClass, Collection<Constraint>> constraints
  ) {
    super(name, description, acl, constraints);

    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkArgument(
      name.length() <= NAME_MAX_LENGTH,
      String.format(
        "System names must not exceed %d characters in length",
        NAME_MAX_LENGTH));
    Preconditions.checkArgument(
      name.matches(NAME_PATTERN),
      "System names must only contain letters, numbers, and hyphens");
  }

  public SystemPolicy(
    @NotNull String name,
    @NotNull String description
  ) {
    this(name, description, null, Map.of());
  }

  public @NotNull SystemPolicy add(@NotNull JitGroupPolicy group) {
    Preconditions.checkArgument(
      !this.groups.containsKey(group.name()),
      "A group with the same name has already been added");

    group.setParent(this);
    this.groups.put(group.name(), group);
    return this;
  }

  public @NotNull Collection<JitGroupPolicy> groups() {
    return Collections.unmodifiableCollection(this.groups.values());
  }

  public @NotNull EnvironmentPolicy environment() {
    Preconditions.checkNotNull(this.parent().isPresent(), "Parent must be set");
    return (EnvironmentPolicy)this.parent().get();
  }

  public Optional<JitGroupPolicy> group(@NotNull String name) {
    return Optional.ofNullable(this.groups.get(name));
  }
}
