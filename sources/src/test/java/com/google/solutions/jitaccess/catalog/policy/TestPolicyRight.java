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

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPolicyRight {
  //---------------------------------------------------------------------------
  // parse.
  //---------------------------------------------------------------------------

  @Test
  public void parseJoin() {
    assertEquals(
      EnumSet.of(PolicyRight.JOIN),
      PolicyRight.parse("Join  "));
  }

  @Test
  public void parseApproveSelf() {
    assertEquals(
      EnumSet.of(PolicyRight.APPROVE_SELF),
      PolicyRight.parse(" approve_self  "));
  }

  @Test
  public void parseApproveOthers() {
    assertEquals(
      EnumSet.of(PolicyRight.APPROVE_OTHERS),
      PolicyRight.parse("APPROVE_OTHERS"));
  }

  @Test
  public void parseList() {
    assertEquals(
      EnumSet.of(PolicyRight.JOIN, PolicyRight.APPROVE_SELF),
      PolicyRight.parse("Join,approve_self,,  "));
  }

  //---------------------------------------------------------------------------
  // toString.
  //---------------------------------------------------------------------------

  @Test
  public void toStringReturnsCanonicalFormat() {
    assertEquals("JOIN", PolicyRight.JOIN.toString());
    assertEquals(
      "[JOIN, APPROVE_OTHERS, APPROVE_SELF]",
      PolicyRight
        .parse("JOIN,approve_self,,approve_self,approve_others  ")
        .toString());
  }
}
