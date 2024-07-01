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
import com.google.solutions.jitaccess.catalog.policy.*;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
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
  public @NotNull Optional<JoinOperation> join() {//TODO: return JoinOperationBuilder, test
    //
    // 1. Try to join with self-approval.
    //
    // NB. Self approval requires that the subject satisfies also approval constraints.
    //
    var joinWithSelfApprovalAnalysis = group
      .analyze(this.catalog.subject(), EnumSet.of(PolicyAccess.JOIN, PolicyAccess.APPROVE_SELF))
      .applyConstraints(Policy.ConstraintClass.JOIN)
      .applyConstraints(Policy.ConstraintClass.APPROVE);
    if (joinWithSelfApprovalAnalysis
      .execute()
      .isAccessAllowed(PolicyAnalysis.AccessOptions.IGNORE_CONSTRAINTS)) {
      //
      // ACL grants access, return details about possibly unsatisfied constraints.
      //
      return Optional.of(new JoinOperation(joinWithSelfApprovalAnalysis));
    }

    //
    // 2. Try to join with approval.
    //
    var joinAnalysis = group
      .analyze(this.catalog.subject(), EnumSet.of(PolicyAccess.JOIN))
      .applyConstraints(Policy.ConstraintClass.JOIN);
    if (joinAnalysis
      .execute()
      .isAccessAllowed(PolicyAnalysis.AccessOptions.IGNORE_CONSTRAINTS)) {
      //
      // ACL grants access, return details about possibly unsatisfied constraints.
      //
      return Optional.of(new JoinOperation(joinAnalysis));
    }

    return Optional.empty();
  }

  // public ApprovalOperation approve(@NotNull String token)
  //TODO: members()


  public class JoinOperation {
    private final @NotNull PolicyAnalysis analysis;

    private JoinOperation(@NotNull PolicyAnalysis analysis) {
      this.analysis = analysis;
    }

    /**
     * @return input required to evaluate constraints.
     */
    public @NotNull List<Property> input() {
      return this.analysis.input();
    }

    /**
     * Perform a "dry run" to check if the join would succeed
     * given the current input.
     */
    public @NotNull PolicyAnalysis.Result dryRun() {
      //
      // Re-run analysis using the latest inputs.
      //
      return this.analysis.execute();
    }

    // analyze (w/ constraints + approval constraints if applicable)
    // build -> request

    // delegateForApproval -> ApprovalOp
    // execute ->
  }

  public class ApprovalOperation {
    // requestingSubject
    // input
    // dryRun (w/ constraints)
    // execute ->

    // from token (-> approver constraints!)
    //   verify different users
  }

  //class JoinOperation {
  //  // execute (w/ constraints)
  //  // delegate -> token
  //}
}
