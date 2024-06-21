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
import java.util.EnumSet;

public enum PolicyRight {
  /**
   * View a group. This right is included in all other rights.
   */
  VIEW(1),
  /**
   * Join a group.
   */
  JOIN(VIEW.value + 2),

  /**
   * Approve someone's request to join a group.
   */
  APPROVE_OTHERS(VIEW.value + 4),

  /**
   * Self-approve.
   */
  APPROVE_SELF(VIEW.value + 8);

  private int value;

  PolicyRight(int value) {
    this.value = value;
  }

  /**
   * @return bit field representation.
   */
  int toMask() {
    return this.value;
  }

  /**
   * @return bit field representation.
   */
  public static int toMask(@NotNull EnumSet<PolicyRight> actions) {
    int mask = 0;
    for (var action : actions) {
      mask |= action.value;
    }
    return mask;
  }

  public static EnumSet<PolicyRight> parse(@NotNull String list) {
    return EnumSet.copyOf(
      Arrays.asList(list.split(","))
        .stream()
        .map(String::trim)
        .map(String::toUpperCase)
        .filter(s -> !s.isBlank())
        .map(PolicyRight::valueOf)
        .toList());
  }
}