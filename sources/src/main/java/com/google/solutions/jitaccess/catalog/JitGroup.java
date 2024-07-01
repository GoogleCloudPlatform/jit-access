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

import com.google.solutions.jitaccess.catalog.auth.GroupId;
import com.google.solutions.jitaccess.catalog.policy.PolicyAnalysis;
import com.google.solutions.jitaccess.catalog.policy.JitGroupPolicy;
import com.google.solutions.jitaccess.catalog.policy.Policy;
import com.google.solutions.jitaccess.catalog.policy.PolicyAccess;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

/**
 * JIT Group in the context of a specific subject.
 */
public class JitGroup {
  private final @NotNull JitGroupPolicy group;
  private final @NotNull Catalog catalog;

  JitGroup(
    @NotNull Catalog catalog,
    @NotNull JitGroupPolicy group
  ) {
    this.catalog = catalog;
    this.group = group;
  }

  /**
   * @return group details.
   */
  public @NotNull JitGroupPolicy group() {
    return this.group;
  }

  /**
   * @return Cloud Identity group that backs this JIT group.
   */
  public @NotNull GroupId cloudIdentityGroupId() {
    return this.catalog.groupMapping().groupFromJitGroup(this.group().id());
  }

  /**
   * @return details about possibly unmet constraints.
   */
  public @NotNull Optional<PolicyAnalysis.Result> analyzeJoinAccess() {//TODO: return JoinOperationBuilder
    //
    // Analyze if the current subject can join this group, and what
    // constraints might be unsatisfied.
    //
    var result = group
      .analyze(this.catalog.subject(), EnumSet.of(PolicyAccess.JOIN))
      .applyConstraints(Policy.ConstraintClass.JOIN)
      .execute();

    // TODO: Attempt JOIN | APPROVE_SELF first (w/ approval consraints), fall back to JOIN

    if (!result.isAccessAllowed(PolicyAnalysis.AccessOptions.IGNORE_CONSTRAINTS)) {
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
