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
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Policy for a JIT group.
 */
public class JitGroupPolicy extends AbstractPolicy {
  /**
   * Maximum length for names, in characters.
   */
  static final int NAME_MAX_LENGTH = 24;
  static final String NAME_PATTERN = "[a-zA-Z0-9\\-]+";

  public JitGroupPolicy(
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
        "JIT group names must not exceed %d characters in length",
        NAME_MAX_LENGTH));
    Preconditions.checkArgument(
      name.matches(NAME_PATTERN),
      "JIT group names must only contain letters, numbers, and hyphens");
  }

  public @NotNull JitGroupId id() {
    return new JitGroupId(
      this.system().environment().name(),
      this.system().name(),
      this.name());
  }

  public @NotNull SystemPolicy system() {
    Preconditions.checkNotNull(this.parent().isPresent(), "Parent must be set");
    return (SystemPolicy)this.parent().get();
  }

  /**
   * Analyze access for a subject.
   */
  public @NotNull PolicyAnalysis analyze(
    @NotNull Subject subject,
    @NotNull EnumSet<PolicyAccess> requiredRights
  ) {
    return new PolicyAnalysis(
      this,
      subject,
      id(),
      requiredRights);
  }
}
