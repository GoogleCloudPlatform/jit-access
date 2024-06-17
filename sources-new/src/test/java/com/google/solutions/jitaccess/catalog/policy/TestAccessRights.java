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
import static org.junit.jupiter.api.Assertions.*;

public class TestAccessRights {
  //---------------------------------------------------------------------------
  // parse.
  //---------------------------------------------------------------------------

  @Test
  public void parseRequest() {
    assertEquals(AccessRights.REQUEST, AccessRights.parse("Request  "));
  }

  @Test
  public void parseApproveSelf() {
    assertEquals(AccessRights.APPROVE_SELF, AccessRights.parse(" approve_self  "));
  }

  @Test
  public void parseApproveOthers() {
    assertEquals(AccessRights.APPROVE_OTHERS, AccessRights.parse("APPROVE_OTHERS"));
  }

  @Test
  public void parseList() {
    assertEquals(
      AccessRights.REQUEST.mask() | AccessRights.APPROVE_SELF.mask(),
      AccessRights.parse("Request,approve_self,,  ").mask());
  }

  //---------------------------------------------------------------------------
  // toString.
  //---------------------------------------------------------------------------

  @Test
  public void toStringReturnsCanonicalFormat() {
    assertEquals("REQUEST", AccessRights.REQUEST.toString());
    assertEquals(
      "REQUEST,APPROVE_OTHERS,APPROVE_SELF",
      AccessRights
        .parse("Request,approve_self,,approve_self,approve_others  ")
        .toString());
  }
}
