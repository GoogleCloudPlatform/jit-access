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

package com.google.solutions.jitaccess.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class TestMoreStrings {

  //---------------------------------------------------------------------------
  // isNullOrBlank.
  //---------------------------------------------------------------------------

  @Test
  public void isNullOrBlank_whenNull() {
    assertTrue(MoreStrings.isNullOrBlank(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ",  "\n\t"})
  public void isNullOrBlank_whenBlank(String s) {
    assertTrue(MoreStrings.isNullOrBlank(s));
  }

  @ParameterizedTest
  @ValueSource(strings = {"x", " x",  "\n\tx"})
  public void isNullOrBlank_whenNotBlank(String s) {
    assertFalse(MoreStrings.isNullOrBlank(s));
  }

  //---------------------------------------------------------------------------
  // quote.
  //---------------------------------------------------------------------------

  @Test
  public void quote() {
    assertEquals("'test'", MoreStrings.quote("test"));
  }
}
