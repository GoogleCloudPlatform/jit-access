//
// Copyright 2023 Google LLC
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

package com.google.solutions.jitaccess.core.activation;

import com.google.common.base.Preconditions;

import java.util.Comparator;

/**
 * An entitlement that a user could activate, or has activated already.
 *
 * @param id
 * @param name
 * @param requirement
 * @param status
 * @param <TEntitlementId>
 */
public record Entitlement<TEntitlementId extends EntitlementId> (
  TEntitlementId id,
  String name,
  Requirement requirement,
  Status status
) implements Comparable<Entitlement<TEntitlementId>> {
  public Entitlement {
    Preconditions.checkNotNull(id, "id");
    Preconditions.checkNotNull(name, "name");
  }

  @Override
  public String toString() {
    return this.name.toString();
  }

  @Override
  public int compareTo(Entitlement<TEntitlementId> o) {
    return Comparator
      .comparing((Entitlement<TEntitlementId> e) -> e.status)
      .thenComparing(e -> e.name)
      .compare(this, o);
  }

  //---------------------------------------------------------------------------
  // Inner classes.
  //---------------------------------------------------------------------------

  public enum Requirement {
    /**
     * Available for self-activation.
     */
    JIT,

    /**
     * Requires approval from another party.
     */
    MPA
  }

  public enum Status {
    /**
     * Entitlement can be activated.
     */
    AVAILABLE,

    /**
     * Entitlement is active.
     */
    ACTIVE,

    /**
     * Approval pending.
     */
    ACTIVATION_PENDING
  }
}
