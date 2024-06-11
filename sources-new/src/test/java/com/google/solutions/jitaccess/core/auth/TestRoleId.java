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

package com.google.solutions.jitaccess.core.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestRoleId {
  // -------------------------------------------------------------------------
  // toString.
  // -------------------------------------------------------------------------

  @Test
  public void toStringReturnsPolicyAndName() {
    Assertions.assertEquals("policy-name", new RoleId("policy", "name").toString());
  }

  // -------------------------------------------------------------------------
  // Equality.
  // -------------------------------------------------------------------------

  @Test
  public void whenObjectAreEquivalent_ThenEqualsReturnsTrue() {
    RoleId id1 = new RoleId("policy", "name");
    RoleId id2 = new RoleId("policy", "name");

    assertTrue(id1.equals(id2));
    assertEquals(id1.hashCode(), id2.hashCode());
    assertEquals(0, id1.compareTo(id2));
  }

  @Test
  public void whenObjectAreEquivalentButDifferInCasing_ThenEqualsReturnsTrue() {
    RoleId id1 = new RoleId("policy", "name");
    RoleId id2 = new RoleId("Policy", "Name");

    assertTrue(id1.equals(id2));
    assertEquals(id1.hashCode(), id2.hashCode());
    assertEquals(0, id1.compareTo(id2));
  }

  @Test
  public void whenObjectAreSame_ThenEqualsReturnsTrue() {
    RoleId id1 = new RoleId("policy", "name");

    assertTrue(id1.equals(id1));
    assertEquals(0, id1.compareTo(id1));
  }

  @Test
  public void whenRolesDiffer_ThenEqualsReturnsFalse() {
    RoleId id1 = new RoleId("policy", "name-1");
    RoleId id2 = new RoleId("policy", "name-2");

    assertFalse(id1.equals(id2));
    assertNotEquals(id1.hashCode(), id2.hashCode());
    assertNotEquals(0, id1.compareTo(id2));
  }

  @Test
  public void whenPoliciesDiffer_ThenEqualsReturnsFalse() {
    RoleId id1 = new RoleId("policy-1", "name");
    RoleId id2 = new RoleId("policy-2", "name");

    assertFalse(id1.equals(id2));
    assertNotEquals(id1.hashCode(), id2.hashCode());
    assertNotEquals(0, id1.compareTo(id2));
  }

  @Test
  public void whenObjectIsNull_ThenEqualsReturnsFalse() {
    RoleId id1 = new RoleId("policy", "name");

    assertFalse(id1.equals(null));
  }

  @Test
  public void whenObjectIsDifferentType_ThenEqualsReturnsFalse() {
    RoleId id1 = new RoleId("policy", "name");

    assertFalse(id1.equals(""));
  }

  // -------------------------------------------------------------------------
  // PrincipalId.
  // -------------------------------------------------------------------------

  @Test
  public void value() {
    assertEquals(
      "policy-name",
      new RoleId("policy", "name").value());
  }
}
