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

package com.google.solutions.jitaccess.catalog.auth;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Identifier of a JIT group.
 */
public class JitGroupId implements Comparable<JitGroupId>, PrincipalId {
  public static final String TYPE = "role";

  // TODO: + environment
  private final @NotNull String policyName;

  private final @NotNull String name;


  public JitGroupId(@NotNull String policyName, @NotNull String name) {
    Preconditions.checkNotNull(policyName, "policyName");
    Preconditions.checkNotNull(name, "roleName");
    Preconditions.checkArgument(!policyName.isBlank());
    Preconditions.checkArgument(!name.isBlank());

    //
    // Use lower-case as canonical format.
    //
    this.policyName = policyName.toLowerCase();
    this.name = name.toLowerCase();
  }

  @Override
  public String toString() {
    return this.value();
  }

  // -------------------------------------------------------------------------
  // Equality.
  // -------------------------------------------------------------------------

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    var that = (JitGroupId)o;
    return
      this.policyName.equals(that.policyName) &&
      this.name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return this.policyName.hashCode() ^ this.name.hashCode();
  }

  @Override
  public int compareTo(@NotNull JitGroupId o) {
    return Comparator
      .comparing((JitGroupId r) -> r.policyName)
      .thenComparing(r -> r.name)
      .compare(this, o);
  }

  // -------------------------------------------------------------------------
  // PrincipalId.
  // -------------------------------------------------------------------------

  @Override
  public @NotNull String type() {
    return TYPE;
  }

  @Override
  public @NotNull String value() {
    return String.format("%s-%s", this.policyName, this.name);
  }
}
