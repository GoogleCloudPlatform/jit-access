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

public class TestPolicyAccess {
  @Test
  public void view() {
    assertTrue((PolicyAccess.JOIN.toMask() & PolicyAccess.VIEW.toMask()) != 0);
  }

  //---------------------------------------------------------------------------
  // parse.
  //---------------------------------------------------------------------------

  @Test
  public void parse_join() {
    assertEquals(
      EnumSet.of(PolicyAccess.JOIN),
      PolicyAccess.parse("Join  "));
  }

  @Test
  public void parse_approveSelf() {
    assertEquals(
      EnumSet.of(PolicyAccess.APPROVE_SELF),
      PolicyAccess.parse(" approve_self  "));
  }

  @Test
  public void parse_approveOthers() {
    assertEquals(
      EnumSet.of(PolicyAccess.APPROVE_OTHERS),
      PolicyAccess.parse("APPROVE_OTHERS"));
  }

  @Test
  public void parse_list() {
    assertEquals(
      EnumSet.of(PolicyAccess.JOIN, PolicyAccess.APPROVE_SELF),
      PolicyAccess.parse("Join,approve_self,,  "));
  }

  //---------------------------------------------------------------------------
  // toString.
  //---------------------------------------------------------------------------

  @Test
  public void toString_returnsCanonicalFormat() {
    assertEquals("JOIN", PolicyAccess.JOIN.toString());
    assertEquals(
      "[JOIN, APPROVE_OTHERS, APPROVE_SELF]",
      PolicyAccess
        .parse("JOIN,approve_self,,approve_self,approve_others  ")
        .toString());
  }
}
