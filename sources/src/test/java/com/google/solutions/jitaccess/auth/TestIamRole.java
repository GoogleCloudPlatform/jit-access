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

import com.google.solutions.jitaccess.TestRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class TestIamRole extends TestRecord<IamRole> {

  @Override
  protected @NotNull IamRole createInstance() {
    return new IamRole("roles/viewer");
  }

  @Override
  protected @NotNull IamRole createDifferentInstance() {
    return new IamRole("roles/editor");
  }

  // -------------------------------------------------------------------------
  // toString.
  // -------------------------------------------------------------------------

  @Test
  public void toString_returnsId() {
    assertEquals("roles/viewer", new IamRole("roles/viewer").toString());
  }

  // -------------------------------------------------------------------------
  // parse.
  // -------------------------------------------------------------------------

  @ParameterizedTest
  @ValueSource(strings = {
    " ",
    "roles/",
    "role/x",
    "ROLES/x",
    "x",
    "organizations/",
    "  projects/  "})
  public void whenRoleInvalid(String s) {
    assertFalse(IamRole.parse(null).isPresent());
    assertFalse(IamRole.parse(s).isPresent());
  }

  @Test
  public void parse_whenPredefinedRole() {
    var role = IamRole.parse("  roles/viewer  ");

    assertTrue(role.isPresent());
    assertEquals("roles/viewer", role.get().name());
  }

  @Test
  public void parse_whenCustomProjectRole() {
    var role = IamRole.parse(" projects/project-1/roles/CustomRole ");

    assertTrue(role.isPresent());
    assertEquals("projects/project-1/roles/CustomRole", role.get().name());
  }

  @Test
  public void parse_whenCustomOrgRole() {
    var role = IamRole.parse(" organizations/123/roles/CustomRole ");

    assertTrue(role.isPresent());
    assertEquals("organizations/123/roles/CustomRole", role.get().name());
  }

  // -------------------------------------------------------------------------
  // isPredefinedRole.
  // -------------------------------------------------------------------------

  @Test
  public void isPredefinedRole() {
    var role = new IamRole("roles/browser");

    assertTrue(role.isPredefined());
    assertFalse(role.isCustomProjectRole());
    assertFalse(role.isCustomOrganizationRole());
  }

  // -------------------------------------------------------------------------
  // isCustomProjectRole.
  // -------------------------------------------------------------------------

  @Test
  public void isCustomProjectRole() {
    var role = new IamRole("projects/my-project/roles/my-role");

    assertFalse(role.isPredefined());
    assertTrue(role.isCustomProjectRole());
    assertFalse(role.isCustomOrganizationRole());
  }

  // -------------------------------------------------------------------------
  // isCustomOrganizationRole.
  // -------------------------------------------------------------------------

  @Test
  public void isCustomOrganizationRole() {
    var role = new IamRole("organizations/123/roles/my-role");

    assertFalse(role.isPredefined());
    assertFalse(role.isCustomProjectRole());
    assertTrue(role.isCustomOrganizationRole());
  }
}
