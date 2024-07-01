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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

abstract class AbstractPolicy implements Policy {
  private final @NotNull String name;
  private final @NotNull String description;
  private final @Nullable Policy parent;
  private final @Nullable AccessControlList acl;
  private final @NotNull Map<ConstraintClass, Collection<Constraint>> constraints;

  protected AbstractPolicy(
    @NotNull String name,
    @NotNull String description,
    @Nullable Policy parent,
    @Nullable AccessControlList acl,
    @NotNull Map<ConstraintClass, Collection<Constraint>> constraints
  ) {
    this.name = name;
    this.description = description;
    this.parent = parent;
    this.acl = acl;
    this.constraints = constraints;
  }

  @NotNull
  @Override
  public String name() {
    return name;
  }

  @NotNull
  @Override
  public String description() {
    return description;
  }

  @Override
  public @NotNull String toString() {
    return this.name;
  }

  @Override
  public @NotNull Optional<Policy> parent() {
    return Optional.ofNullable(this.parent);
  }

  @Override
  public @NotNull Optional<AccessControlList> accessControlList() {
    return Optional.ofNullable(acl);
  }

  @Override
  public @NotNull Collection<Constraint> constraints(ConstraintClass c) {
    var constraints = this.constraints.get(c);
    return constraints != null
      ? constraints
      : List.of();
  }
}
