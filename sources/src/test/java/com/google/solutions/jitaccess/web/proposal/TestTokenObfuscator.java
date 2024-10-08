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

package com.google.solutions.jitaccess.web.proposal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTokenObfuscator {
  @Test
  public void encode() {
    var jwt = "eyABC.eyDE.FG";

    assertEquals("ABC~~DE~FG", TokenObfuscator.encode(jwt));
  }

  @Test
  public void roundtrip() {
    var jwt = "eyABC.eyDE.FG";

    assertEquals(jwt, TokenObfuscator.decode(TokenObfuscator.encode(jwt)));
  }
}
