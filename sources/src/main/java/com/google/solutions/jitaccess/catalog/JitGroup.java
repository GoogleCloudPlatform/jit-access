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

package com.google.solutions.jitaccess.catalog;

import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.AccessCheck;
import com.google.solutions.jitaccess.catalog.policy.JitGroupPolicy;
import com.google.solutions.jitaccess.catalog.policy.Policy;
import com.google.solutions.jitaccess.catalog.policy.PolicyRight;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

/**
 * JIT Group in the context of a specific subject.
 */
public class JitGroup {
  private final @NotNull Subject subject;
  private final @NotNull JitGroupPolicy group;

  JitGroup(@NotNull Subject subject, @NotNull JitGroupPolicy group) {
    this.subject = subject;
    this.group = group;
  }

  /**
   * @return group details.
   */
  public @NotNull JitGroupPolicy group() {
    return this.group;
  }

  /**
   * @return details about possibly unmet constraints.
   */
  public @NotNull Optional<AccessCheck.Result> analyzeJoinAccess() {//TODO: replace with join request
    //
    // Analyze if the current subject can join this group, and what
    // constraints might be unsatisfied.
    //
    var result = group
      .createAccessCheck(this.subject, EnumSet.of(PolicyRight.JOIN))
      .applyConstraints(Policy.ConstraintClass.JOIN)
      .execute();

    if (!result.isSubjectInAcl()) {
      //
      // Subject not in ACL, so we can't disclose any details.
      //
      return Optional.empty();
    }
    else {
      return Optional.of(result);
    }
  }

  //TODO: members()


  public @NotNull Optional<JoinOperationBuilder> join() {
    // check ACL w/o constraints
    // if ok, return request
    throw new RuntimeException("NIY");
  }

  class JoinOperationBuilder {
    // input
    // analyze (w/ constraints + approval constraints if applicable)
    // build -> request

    // build -> JoinOperation
  }

  class DelegatedJoinOperationBuilder {
    // input
    // analyze (w/ constraints)
    // build -> JoinOperation

    // from token (-> approver constraints!)
    //   verify different users
  }

  class JoinOperation {
    // execute (w/ constraints)
    // delegate -> token
  }
}
