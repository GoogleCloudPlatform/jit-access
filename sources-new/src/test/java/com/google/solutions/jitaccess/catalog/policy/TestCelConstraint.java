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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestCelConstraint {
  //---------------------------------------------------------------------------
  // toString.
  //---------------------------------------------------------------------------

  @Test
  public void toStringReturnsName() {
    var constraint = new CelConstraint("name", "display name", Map.of(), "?");
    assertEquals("name [?]", constraint.toString());
  }

  //---------------------------------------------------------------------------
  // createCheck.
  //---------------------------------------------------------------------------

  @Test
  public void whenExpressionInvalid_ThenCheckThrowsException() throws Exception {
    var constraint = new CelConstraint(
      "name",
      "display name",
      Map.of(),
      "my.name == 'missing quote");

    assertThrows(
      CelConstraint.InvalidExpressionException.class,
      () -> constraint.createCheck().execute());
  }

  @Test
  public void whenContextVariableSet_ThenCheckCanAccessVariable() throws Exception {
    var constraint = new CelConstraint(
      "name",
      "display name",
      Map.of(),
      "my.name == 'test'");

    var positive = constraint.createCheck();
    positive.addContext("my").set("name", "test");
    assertTrue(positive.execute());

    var negative = constraint.createCheck();
    negative.addContext("my").set("name", "foo");
    assertFalse(negative.execute());
  }

  @Test
  public void whenExpressionContainsExtractCall_ThenCheckSucceeds() throws Exception {
    var constraint = new CelConstraint(
      "name",
      "display name",
      Map.of(),
      "my.name.extract('t{x}t') == 'es'");

    var positive = constraint.createCheck();
    positive.addContext("my").set("name", "test");
    assertTrue(positive.execute());
  }
}
