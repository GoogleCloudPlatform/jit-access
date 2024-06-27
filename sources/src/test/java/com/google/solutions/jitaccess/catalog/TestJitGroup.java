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

import com.google.solutions.jitaccess.catalog.JitGroup;
import com.google.solutions.jitaccess.catalog.auth.Principal;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.auth.UserId;
import com.google.solutions.jitaccess.catalog.policy.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TestJitGroup {
  private static final UserId SAMPLE_USER = new UserId("user@example.com");

  private static Constraint createFailingConstraint() throws ConstraintException {
    var check = Mockito.mock(Constraint.Check.class);
    when(check.execute())
      .thenThrow(new IllegalStateException("Mock"));
    when(check.addContext(anyString()))
      .thenReturn(Mockito.mock(Constraint.Context.class));

    var constraint = Mockito.mock(Constraint.class);
    when(constraint.createCheck())
      .thenReturn(check);

    return constraint;
  }

  // -------------------------------------------------------------------------
  // analyzeJoinAccess.
  // -------------------------------------------------------------------------

  @Test
  public void analyzeJoinAccess_whenNotAllowed_thenReturnsEmpty() {
    var subject = Mockito.mock(Subject.class);
    when(subject.principals())
      .thenReturn(Set.of(new Principal(SAMPLE_USER)));

    var catalog = Mockito.mock(Catalog.class);
    when(catalog.subject())
      .thenReturn(subject);

    var environment = new EnvironmentPolicy("env-1", "Environment 1");
    var system = new SystemPolicy(environment, "system-1", "System 1");
    var deniedGroup = new JitGroupPolicy(
      system,
      "group-1",
      "Group 1",
      new AccessControlList(
        List.of(new AccessControlList.AllowedEntry(
          SAMPLE_USER,
          PolicyRight.VIEW.toMask()))), // missing JOIN
      Map.of());

    var group = new JitGroup(catalog, deniedGroup);

    assertFalse(group.analyzeJoinAccess().isPresent());
  }

  @Test
  public void analyzeJoinAccess_whenAllowedButConstraintFails_thenReturnsDetails() throws Exception {
    var subject = Mockito.mock(Subject.class);
    when(subject.user())
      .thenReturn(SAMPLE_USER);
    when(subject.principals())
      .thenReturn(Set.of(new Principal(SAMPLE_USER)));

    var catalog = Mockito.mock(Catalog.class);
    when(catalog.subject())
      .thenReturn(subject);

    var environment = new EnvironmentPolicy("env-1", "Environment 1");
    var system = new SystemPolicy(environment, "system-1", "System 1");
    var deniedGroup = new JitGroupPolicy(
      system,
      "group-1",
      "Group 1",
      new AccessControlList(
        List.of(new AccessControlList.AllowedEntry(
          SAMPLE_USER,
          PolicyRight.JOIN.toMask()))),
      Map.of(Policy.ConstraintClass.JOIN, List.of(createFailingConstraint())));

    var access = new JitGroup(catalog, deniedGroup).analyzeJoinAccess();

    assertTrue(access.isPresent());
    assertTrue(access.get().isSubjectInAcl());
    assertEquals(0, access.get().satisfiedConstraints().size());
    assertEquals(1, access.get().unsatisfiedConstraints().size());
    assertEquals(1, access.get().failedConstraints().size());
  }
}
