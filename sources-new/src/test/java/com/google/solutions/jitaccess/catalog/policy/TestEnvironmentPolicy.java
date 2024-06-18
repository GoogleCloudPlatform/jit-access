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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestEnvironmentPolicy {

  //---------------------------------------------------------------------------
  // Constructor.
  //---------------------------------------------------------------------------

  @ParameterizedTest
  @ValueSource(strings = {
    " ",
    "123456789_1234567",
    "with spaces",
    "?"})
  public void whenNameInvalid_ThenConstructorThrowsException(String name) {
    assertThrows(
      IllegalArgumentException.class,
      () -> new EnvironmentPolicy(name, "description"));
  }

  //---------------------------------------------------------------------------
  // toString.
  //---------------------------------------------------------------------------

  @Test
  public void toStringReturnsName() {
    var environment = new EnvironmentPolicy("env", "");
    assertEquals("env", environment.toString());
  }

  //---------------------------------------------------------------------------
  // system.
  //---------------------------------------------------------------------------

  @Test
  public void system() {
    var environment = new EnvironmentPolicy("env", "");
    var system = new SystemPolicy(environment, "system-1", "");

    assertTrue(environment.system("system-1").isPresent());
    assertFalse(environment.system("system-2").isPresent());
  }
}
