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

import java.util.Arrays;

/**
 * Right to perform a certain action, intended to be used
 * in ACLs.
 *
 * Implemented as a class (instead of enum) to allow
 * easier bit field manipulation.
 */
public class AccessRights {
  /**
   * Request to activate a role.
   */
  public static final AccessRights REQUEST =  new AccessRights(1);

  /**
   * Approve activation requests from other users.
   */
  public static final AccessRights APPROVE_OTHERS = new AccessRights(2);

  /**
   * Self-approve activation requests.
   */
  public static final AccessRights APPROVE_SELF = new AccessRights(4);

  /**
   * Bit field.
   */
  private final int mask;

  private AccessRights(int mask) {
    this.mask = mask;
  }

  /**
   * @return bit field representation.
   */
  int mask() {
    return this.mask;
  }

  static AccessRights parse(@NotNull String s) {
    var elements = s.split(",");
    if (elements.length == 1) {
      switch (s.trim().toUpperCase()) {
        case "REQUEST":
          return REQUEST;
        case "APPROVE_OTHERS":
          return APPROVE_OTHERS;
        case "APPROVE_SELF":
          return APPROVE_SELF;
        default:
          throw new IllegalArgumentException(
            "Unrecognized access right: " + s);
      }
    }
    else {
      return new AccessRights(Arrays.stream(elements)
        .filter(e -> !e.isBlank())
        .map(AccessRights::parse)
        .map(ar -> ar.mask)
        .reduce(0, (lhs, rhs) -> lhs | rhs));
    }
  }
}
