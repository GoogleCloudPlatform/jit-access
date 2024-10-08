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

package com.google.solutions.jitaccess.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

/**
 * Principal ID that is recognized by IAM.
 */
public interface IamPrincipalId extends PrincipalId, Comparable<IamPrincipalId> {

  /**
   * Parse a user ID that uses the syntax type:id.
   */
  static Optional<IamPrincipalId> parse(@Nullable String s) {
    return Optional.<IamPrincipalId>empty()
      .or(() -> EndUserId.parse(s))
      .or(() -> GroupId.parse(s))
      .or(() -> ServiceAccountId.parse(s));
  }

  default int compareTo(@NotNull IamPrincipalId o) {
    return Comparator
      .comparing((IamPrincipalId r) -> r.type())
      .thenComparing(r -> r.value())
      .compare(this, o);
  }
}
