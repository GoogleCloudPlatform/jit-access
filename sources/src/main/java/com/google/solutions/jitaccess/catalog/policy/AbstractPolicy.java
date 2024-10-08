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
import com.google.solutions.jitaccess.auth.AbstractSecurableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base implementation for a policy.
 */
abstract class AbstractPolicy extends AbstractSecurableComponent implements Policy {
  private final @NotNull String name;
  private final @NotNull String displayName;
  private final @NotNull String description;
  private @Nullable Policy parent;
  private final @Nullable AccessControlList acl;
  private final @NotNull Map<ConstraintClass, Collection<Constraint>> constraints;

  protected AbstractPolicy(
    @NotNull String name,
    @NotNull String description,
    @Nullable AccessControlList acl,
    @NotNull Map<ConstraintClass, Collection<Constraint>> constraints
  ) {
    Preconditions.checkArgument(
      name != null && !name.isBlank(),
      "The policy must have a name");
    this.name = name.toLowerCase();
    this.displayName = name;
    this.description = description;
    this.acl = acl;
    this.constraints = constraints;
  }

  //---------------------------------------------------------------------------
  // AbstractSecurableComponent overrides.
  //---------------------------------------------------------------------------

  /**
   * Access control list.
   */
  @Override
  protected @NotNull Optional<AccessControlList> accessControlList() {
    return Optional.ofNullable(this.acl);
  }

  /**
   * Return parent policy as container so that we inherit its ACL.
   */
  @Override
  protected @NotNull Optional<? extends AbstractSecurableComponent> container() {
    return Optional.ofNullable((AbstractSecurableComponent)this.parent);
  }

  //---------------------------------------------------------------------------
  // Public methods.
  //---------------------------------------------------------------------------

  /**
   * Name of policy.
   */
  @NotNull
  @Override
  public String name() {
    return this.name;
  }

  /**
   * Display name of policy.
   */
  @NotNull
  @Override
  public String displayName() {
    return this.displayName;
  }

  /**
   * Description of the policy, for informational purposes only.
   */
  @NotNull
  @Override
  public String description() {
    return this.description;
  }

  /**
   * Parent policy, if any.
   * <p>
   * If a policy has a parent, the parent's ACL and constraints
   * are inherited, with the current policy taking precedence.
   */
  @Override
  public @NotNull Optional<Policy> parent() {
    return Optional.ofNullable(this.parent);
  }

  /**
   * Raw map of constraints.
   */
  Map<ConstraintClass, Collection<Constraint>> constraints() {
    return this.constraints;
  }

  /**
   * List of constraints.
   * <p>
   * Constraints must have a unique name. If a parent and child policy
   * both contain a constraint with the same class and name, the child's
   * policy's constraint takes priority.
   */
  @Override
  public @NotNull Collection<Constraint> constraints(@NotNull ConstraintClass c) {
    var constraints = this.constraints.get(c);
    return constraints != null
      ? constraints
      : List.of();
  }

  /**
   * Set the parent. This method can only be called once, and should only
   * be used during initialization.
   */
  protected void setParent(@NotNull Policy parent) {
    Preconditions.checkArgument(parent != this, "Parent must not be the same policy");
    Preconditions.checkArgument(this.parent == null, "Parent has been set already");
    this.parent = parent;
  }

  @Override
  public @NotNull String toString() {
    return this.name;
  }
}
