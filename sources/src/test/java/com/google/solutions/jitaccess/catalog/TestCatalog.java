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

import com.google.solutions.jitaccess.apis.clients.AccessDeniedException;
import com.google.solutions.jitaccess.catalog.auth.*;
import com.google.solutions.jitaccess.catalog.policy.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class TestCatalog {
  private static final UserId SAMPLE_USER = new UserId("user@example.com");

  // -------------------------------------------------------------------------
  // environment.
  // -------------------------------------------------------------------------

  @Test
  public void environment() {
    var catalog = new Catalog(
      Mockito.mock(Subject.class),
      Map.of("env-1", new EnvironmentPolicy("env-1", "Environment 1")),
      Mockito.mock(GroupMapping.class));

    assertFalse(catalog.environment("").isPresent());
    assertFalse(catalog.environment("ENV-1").isPresent());

    assertTrue(catalog.environment("env-1").isPresent());
  }

  // -------------------------------------------------------------------------
  // environments.
  // -------------------------------------------------------------------------

  @Test
  public void environments() {
    var catalog = new Catalog(
      Mockito.mock(Subject.class),
      Map.of(
        "env-1", new EnvironmentPolicy("env-1", "Environment 1"),
        "env-2", new EnvironmentPolicy("env-2", "Environment 2")),
      Mockito.mock(GroupMapping.class));

    assertEquals(2, catalog.environments().size());
  }

  // -------------------------------------------------------------------------
  // group.
  // -------------------------------------------------------------------------

  @Test
  public void group_whenInvalid_thenThrowsExeption() {
    var environment = new EnvironmentPolicy("env-1", "Environment 1");
    var system = new SystemPolicy(environment, "system-1", "System 1");

    var catalog = new Catalog(
      Mockito.mock(Subject.class),
      Map.of(environment.name(), environment),
      Mockito.mock(GroupMapping.class));

    assertThrows(
      AccessDeniedException.class,
      () -> catalog.group(new JitGroupId("env-1", "system-1", "group-1")));
  }

  @Test
  public void group_whenNotAllowed_thenThrowsExeption() {
    var environment = new EnvironmentPolicy("env-1", "Environment 1");
    var system = new SystemPolicy(environment, "system-1", "System 1");
    var group = new JitGroupPolicy(
      system,
      "group-1",
      "Group 1",
      new AccessControlList(List.of()), // Empty ACL -> deny all
      Map.of());

    var catalog = new Catalog(
      Mockito.mock(Subject.class),
      Map.of(environment.name(), environment),
      Mockito.mock(GroupMapping.class));

    assertThrows(
      AccessDeniedException.class,
      () -> catalog.group(group.id()));
  }

  @Test
  public void group_whenAllowed_thenReturnsDetails() throws Exception {
    var subject = Mockito.mock(Subject.class);
    when(subject.principals())
      .thenReturn(Set.of(new Principal(SAMPLE_USER)));

    var environment = new EnvironmentPolicy("env-1", "Environment 1");
    var system = new SystemPolicy(environment, "system-1", "System 1");
    var group = new JitGroupPolicy(
      system,
      "group-1",
      "Group 1",
      new AccessControlList(
        List.of(new AccessControlList.AllowedEntry(
          SAMPLE_USER,
          PolicyAccess.VIEW.toMask()))),
      Map.of());

    var catalog = new Catalog(
      subject,
      Map.of(environment.name(), environment),
      Mockito.mock(GroupMapping.class));

    var details = catalog.group(group.id());
    assertNotNull(details);
    assertEquals(group, details.group());
  }

  // -------------------------------------------------------------------------
  // group.
  // -------------------------------------------------------------------------

  @Test
  public void groups_whenSomeGroupsNotAllowed_thenResultIsFiltered() {
    var subject = Mockito.mock(Subject.class);
    when(subject.principals())
      .thenReturn(Set.of(new Principal(SAMPLE_USER)));

    var environment = new EnvironmentPolicy("env-1", "Environment 1");
    var system = new SystemPolicy(environment, "system-1", "System 1");
    var allowedGroup = new JitGroupPolicy(
      system,
      "allowed-1",
      "Group 1",
      new AccessControlList(
        List.of(new AccessControlList.AllowedEntry(
          SAMPLE_USER,
          PolicyAccess.VIEW.toMask()))),
      Map.of());
    var deniedGroup = new JitGroupPolicy(
      system,
      "group-1",
      "Group 1",
      new AccessControlList(List.of()), // Empty ACL -> deny all
      Map.of());

    var catalog = new Catalog(
      subject,
      Map.of(environment.name(), environment),
      Mockito.mock(GroupMapping.class));

    var groups = catalog.groups(environment.name());
    assertEquals(1, groups.size());
    assertSame(allowedGroup, groups.stream().findFirst().get().group());
  }

}
