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
  public void whenPolicyHasNoAcl_ThenAccessAllowedByAcl() {
    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());

    var subject = createSubject(SAMPLE_USER, Set.of());

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    assertTrue(check.execute().isSubjectInAcl());
  }

  @Test
  public void isSubjectInAcl_whenPolicyHasEmptyAcl() {
    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList())
      .thenReturn(Optional.of(new AccessControlList(List.of())));

    var subject = createSubject(SAMPLE_USER, Set.of());

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    assertFalse(check.execute().isSubjectInAcl());
  }

  @Test
  public void isSubjectInAcl_whenPolicyHasNoAclAndEmptyParentAcl() {
    var parentPolicy = Mockito.mock(Policy.class);
    when(parentPolicy.parent()).thenReturn(Optional.empty());
    when(parentPolicy.accessControlList())
      .thenReturn(Optional.of(new AccessControlList(List.of())));

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.of(parentPolicy));
    when(policy.accessControlList()).thenReturn(Optional.empty());

    var subject = createSubject(SAMPLE_USER, Set.of());

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    assertFalse(check.execute().isSubjectInAcl());
  }

  @Test
  public void isSubjectInAcl_whenPolicyAclAllowsAccess() {
    var acl = new AccessControlList(List.of(
      new AccessControlList.AllowedEntry(
        SAMPLE_USER,
        PolicyRight.JOIN.toMask())));

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.of(acl));

    var subject = createSubject(SAMPLE_USER, Set.of());

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    assertTrue(check.execute().isSubjectInAcl());
  }

  @Test
  public void isSubjectInAcl_whenPolicyAclDeniesAccess() {
    var acl = new AccessControlList(List.of(
      new AccessControlList.AllowedEntry( // Irrelevant ACE
        SAMPLE_USER,
        PolicyRight.APPROVE_SELF.toMask()),
      new AccessControlList.DeniedEntry(
        SAMPLE_USER,
        PolicyRight.JOIN.toMask())));

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.of(acl));

    var subject = createSubject(SAMPLE_USER, Set.of());

    var check = new AccessCheck(
      policy,
      subject,
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));

    assertFalse(check.execute().isSubjectInAcl());
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

    assertFalse(check.execute().isMembershipActive());
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

    assertTrue(check.execute().isMembershipActive());
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
    "input.testString == 'sample'"
  })
  public void constraints_whenConstraintSatisfied(String expression) {
    var constraint = new CelConstraint(
      "cel",
      "",
      List.of(
        new CelConstraint.Variable("testInt", "", int.class),
        new CelConstraint.Variable("testString", "", String.class)),
      expression);

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
    check.applyConstraints(List.of(constraint));
    check.input().get(0).set("42");
    check.input().get(1).set("sample");

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

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
    check.applyConstraints(List.of(constraint));
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

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
    check.applyConstraints(List.of(constraint));
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
      List.of(),
      "true");

    var policy = Mockito.mock(Policy.class);
    when(policy.parent()).thenReturn(Optional.empty());
    when(policy.accessControlList()).thenReturn(Optional.empty());

    var check = new AccessCheck(
      policy,
      createSubject(SAMPLE_USER, Set.of()),
      SAMPLE_GROUPID,
      EnumSet.of(PolicyRight.JOIN));
  }
}
