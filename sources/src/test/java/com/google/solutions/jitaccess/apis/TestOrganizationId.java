//
// Copyright 2022 Google LLC
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

package com.google.solutions.jitaccess.apis;

import com.google.solutions.jitaccess.TestRecord;
import com.google.solutions.jitaccess.apis.clients.ResourceManagerClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class TestOrganizationId extends TestRecord<OrganizationId> {

  @Override
  protected @NotNull OrganizationId createInstance() {
    return new OrganizationId("100000000000000001");
  }

  @Override
  protected @NotNull OrganizationId createDifferentInstance() {
    return new OrganizationId("100000000000000002");
  }

  // -------------------------------------------------------------------------
  // toString.
  // -------------------------------------------------------------------------

  @Test
  public void toString_returnsId() {
    assertEquals("100000000000000001", new OrganizationId("100000000000000001").toString());
  }

  // -------------------------------------------------------------------------
  // parse.
  // -------------------------------------------------------------------------

  @Test
  public void parse_whenIdPrefixed() {
    var id = OrganizationId.parse(OrganizationId.PREFIX + "100000000000000001");

    assertTrue(id.isPresent());
    assertEquals("100000000000000001", id.get().toString());
  }

  @Test
  public void parse_whenIdNotPrefixed() {
    var id = OrganizationId.parse(" 100000000000000001 ");

    assertTrue(id.isPresent());
    assertEquals("100000000000000001", id.get().toString());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    " ",
    "name",
    "foo/bar",
    "100000000000000001/",
    OrganizationId.PREFIX,
    "organizations//1",
    "organizations//1/resource/b"
  })
  public void parse_whenIdInvalid(String s) {
    assertFalse(OrganizationId.parse(null).isPresent());
    assertFalse(OrganizationId.parse(s).isPresent());
  }

  // -------------------------------------------------------------------------
  // Service.
  // -------------------------------------------------------------------------

  @Test
  public void service() {
    assertEquals(ResourceManagerClient.SERVICE, new OrganizationId("100000000000000001").service());
  }

  // -------------------------------------------------------------------------
  // Type.
  // -------------------------------------------------------------------------

  @Test
  public void type() {
    assertEquals("organization", new OrganizationId("100000000000000001").type());
  }

  // -------------------------------------------------------------------------
  // ID.
  // -------------------------------------------------------------------------

  @Test
  public void id() {
    assertEquals("100000000000000001", new OrganizationId("100000000000000001").id());
  }

  // -------------------------------------------------------------------------
  // Path.
  // -------------------------------------------------------------------------

  @Test
  public void path() {
    assertEquals("organizations/100000000000000001", new OrganizationId("100000000000000001").path());
  }

  // -------------------------------------------------------------------------
  // Comparable.
  // -------------------------------------------------------------------------

  @Test
  public void compareTo() {
    var projects = List.of(
      new OrganizationId("10003"),
      new OrganizationId("10001"),
      new OrganizationId("10002"));

    assertIterableEquals(
      List.of(
        new OrganizationId("10001"),
        new OrganizationId("10002"),
        new OrganizationId("10003")),
      new TreeSet<>(projects));
  }
}
