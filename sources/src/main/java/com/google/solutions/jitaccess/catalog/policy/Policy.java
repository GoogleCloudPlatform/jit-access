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

import com.google.solutions.jitaccess.catalog.auth.Subject;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

public interface Policy {
  /**
   * @return parent policy, if any.
   */
  @NotNull Optional<Policy> parent();

  /**
   * @return ACL, if any. A policy without an ACL grants access to all principals.
   */
  @NotNull Optional<AccessControlList> accessControlList();

  /**
   * @return constraints, if any.
   */
  @NotNull Collection<Constraint> constraints(ConstraintClass action);

  public enum ConstraintClass {
    JOIN,
    APPROVE,
    RECERTIFY
  }

  /**
   * Check access based on this policy's ACL, and it's ancestry's ACLs.
   */
  default boolean checkAccess(
    @NotNull Subject subject,
    @NotNull EnumSet<PolicyAccess> requiredRights
  ) {
    //
    // Evaluate parent policy.
    //
    if (this.parent().isPresent() &&
      !this.parent().get().checkAccess(subject, requiredRights)) {

      //
      // Parent denies access.
      //
      return false;
    }

    if (this.accessControlList().isPresent() &&
      !this.accessControlList()
        .get()
        .isAllowed(subject, PolicyAccess.toMask(requiredRights))) {

      //
      // This policy's ACL denies access.
      //
      return false;
    }

    return true;
  }
}
