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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Policy for an environment such as "prod".
 */
public record EnvironmentPolicy(
  @NotNull String name,
  @NotNull String description,
  @NotNull List<SystemPolicy> systems
) implements Policy {
  /**
   * Maximum length for names, in characters.
   */
  static final int NAME_MAX_LENGTH = 16;
  static final String NAME_PATTERN = "[a-zA-Z0-9\\-]+";

  public EnvironmentPolicy {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkArgument(
      name.length() <= NAME_MAX_LENGTH,
      String.format(
        "Environment names must not exceed %d characters in length",
        NAME_MAX_LENGTH));
    Preconditions.checkArgument(
      name.matches(NAME_PATTERN),
      "Environment names must only contain letters, numbers, and hyphens");
  }

  public EnvironmentPolicy(
    @NotNull String name,
    @NotNull String description
  ) {
    this(name, description, new LinkedList<>());
  }

  public @NotNull Optional<SystemPolicy> system(@NotNull String name) {
    return this.systems
      .stream()
      .filter(s -> s.name().equals(name))
      .findFirst();
  }

  @Override
  public @NotNull String toString() {
    return this.name;
  }

  @Override
  public @NotNull Optional<Policy> parent() {
    return Optional.empty();
  }

  @Override
  public @NotNull Optional<AccessControlList> accessControlList() {
    return Optional.empty();
  }

  @Override
  public @NotNull Collection<Constraint> constraints(ConstraintClass c) {
    return List.of();
  }
}
