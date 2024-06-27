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

import com.google.solutions.jitaccess.catalog.auth.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TestAccessCheck {
  private static final UserId SAMPLE_USER = new UserId("user@example.com");
  private static final JitGroupId SAMPLE_GROUPID = new JitGroupId("env-1", "system-1", "group-1");

  private static Subject createSubject(
    UserId user,
    Set<PrincipalId> otherPrincipals
  ) {
    var subject = Mockito.mock(Subject.class);
    when(subject.user()).thenReturn(user);
    when(subject.principals()).thenReturn(
        Stream.concat(otherPrincipals.stream(), Stream.<PrincipalId>of(user))
          .map(p -> new Principal(p))
          .collect(Collectors.toSet()));

    return subject;
  }

  //---------------------------------------------------------------------------
  // ACL check.
  //---------------------------------------------------------------------------

  @Test
  public void isSubjectInAcl_whenPolicyDeniesAccess() {

    var subject = createSubject(SAMPLE_USER, Set.of());
    var policy = Mockito.mock(Policy.class);
    when(policy.checkAccess(subject, EnumSet.of(PolicyRight.JOIN)))
      .thenReturn(false);

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    assertFalse(check.execute().isSubjectInAcl());
  }

  @Test
  public void isSubjectInAcl_whenPolicyGrantsAccess() {

    var subject = createSubject(SAMPLE_USER, Set.of());
    var policy = Mockito.mock(Policy.class);
    when(policy.checkAccess(subject, EnumSet.of(PolicyRight.JOIN)))
      .thenReturn(true);

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    assertTrue(check.execute().isSubjectInAcl());
  }

  //---------------------------------------------------------------------------
  // Membership check.
  //---------------------------------------------------------------------------

  @Test
  public void isMembershipActive_whenSubjectLacksPrincipal() {
    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());

    var subject = createSubject(SAMPLE_USER, Set.of());

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    var result = check.execute();
    assertFalse(result.activeMembership().isPresent());
  }

  @Test
  public void isMembershipActive_whenSubjectHasPrincipal() {
    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());

    var subject = createSubject(SAMPLE_USER, Set.of(SAMPLE_GROUPID));

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    var result = check.execute();
    assertTrue(result.activeMembership().isPresent());
    assertSame(SAMPLE_GROUPID, result.activeMembership().get().id());
  }

  //---------------------------------------------------------------------------
  // Constraints check.
  //---------------------------------------------------------------------------

  @Test
  public void constraints_whenPolicyHasNoConstraints() {
    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
    var result = check.execute();

    assertTrue(result.satisfiedConstraints().isEmpty());
    assertTrue(result.unsatisfiedConstraints().isEmpty());
    assertTrue(result.failedConstraints().isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "subject.email=='user@example.com'",
    "size(subject.principals) > 0",
    "group.environment == 'env-1'",
    "group.system == 'system-1'",
    "group.name == 'group-1'",
    "input.testInt == 42",
    "input.testString == 'sample'",
    "input.testBoolean"
  })
  public void constraints_whenConstraintSatisfied(String expression) {
    var constraint = new CelConstraint(
      "cel",
      "",
      List.of(
        new CelConstraint.IntegerVariable("testInt", "", 41, 42),
        new CelConstraint.StringVariable("testString", "", 0 ,10),
        new CelConstraint.BooleanVariable("testBoolean", "")),
      expression);

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());
    when(policy.constraints(eq(Policy.ConstraintClass.JOIN)))
      .thenReturn(List.of(constraint));

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
    check.applyConstraints(Policy.ConstraintClass.JOIN);
    check.input().get(0).set("42");
    check.input().get(1).set("sample");
    check.input().get(2).set("True");

    var result = check.execute();

    assertEquals(1, result.satisfiedConstraints().size());
    assertTrue(result.unsatisfiedConstraints().isEmpty());
    assertTrue(result.failedConstraints().isEmpty());
  }

  @Test
  public void constraints_whenConstraintUnsatisfied() {
    var constraint = new CelConstraint(
      "cel",
      "",
      List.of(),
      "false");

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());
    when(policy.constraints(eq(Policy.ConstraintClass.JOIN)))
      .thenReturn(List.of(constraint));

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
    check.applyConstraints(Policy.ConstraintClass.JOIN);
    var result = check.execute();

    assertTrue(result.satisfiedConstraints().isEmpty());
    assertEquals(1, result.unsatisfiedConstraints().size());
    assertTrue(result.failedConstraints().isEmpty());
  }

  @Test
  public void constraints_whenConstraintFails() {
    var constraint = new CelConstraint(
      "cel",
      "",
      List.of(),
      "syntax error(");

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());
    when(policy.constraints(eq(Policy.ConstraintClass.JOIN)))
      .thenReturn(List.of(constraint));

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
    check.applyConstraints(Policy.ConstraintClass.JOIN);
    var result = check.execute();

    assertTrue(result.satisfiedConstraints().isEmpty());
    assertEquals(1, result.unsatisfiedConstraints().size());
    assertEquals(1, result.failedConstraints().size());
    assertNotNull(result.failedConstraints().get(constraint));
  }

  @Test
  public void evaluateConstraint_whenInputMissing() {
    var constraint = new CelConstraint(
      "cel",
      "",
      List.of(new CelConstraint.StringVariable("test", "", 0, 10)),
      "true");

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());
    when(policy.constraints(eq(Policy.ConstraintClass.JOIN)))
      .thenReturn(List.of(constraint));

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    check.applyConstraints(Policy.ConstraintClass.JOIN);
    var result = check.execute();

    assertTrue(result.satisfiedConstraints().isEmpty());
    assertEquals(1, result.unsatisfiedConstraints().size());
    assertEquals(1, result.failedConstraints().size());
    assertNotNull(result.failedConstraints().get(constraint));
  }
}
