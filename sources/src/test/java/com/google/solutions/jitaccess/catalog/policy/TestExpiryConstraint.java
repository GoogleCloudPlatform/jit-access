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

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class TestExpiryConstraint {
  private static final ExpiryConstraint FIXED =
    new ExpiryConstraint(Duration.ofMinutes(1), Duration.ofMinutes(1));

  private static final ExpiryConstraint USER_DEFINED =
    new ExpiryConstraint(Duration.ofMinutes(1), Duration.ofDays(3));

  //---------------------------------------------------------------------------
  // isFixedDuration.
  //---------------------------------------------------------------------------

  @Test
  public void isFixedDuration() {
    assertTrue(FIXED.isFixedDuration());
    assertFalse(USER_DEFINED.isFixedDuration());
  }

  //---------------------------------------------------------------------------
  // description.
  //---------------------------------------------------------------------------

  @Test
  public void description_whenFixedDuration() {
    assertEquals("Membership expires after 1 minute", FIXED.description());
  }

  @Test
  public void description_whenUserDefinedDuration() {
    assertEquals("You must choose an expiry between 1 minute and 3 days", USER_DEFINED.description());
  }

  //---------------------------------------------------------------------------
  // createCheck.
  //---------------------------------------------------------------------------

  @Test
  public void createCheck_whenFixedDuration_thenCheckSucceeds() throws Exception {
    var check = FIXED.createCheck();

    assertSame(FIXED, check.constraint());
    assertEquals(0, check.input().size());
    assertTrue(check.execute());
  }

  @Test
  public void createCheck_whenUserDefinedDurationAndInputMissing_thenCheckFails() throws Exception {
    var check = USER_DEFINED.createCheck();

    assertSame(USER_DEFINED, check.constraint());
    assertEquals(1, check.input().size());
    assertFalse(check.execute());
  }

  @Test
  public void createCheck_whenUserDefinedDurationAndInputInvalid_thenThrowsException() throws Exception {
    var check = USER_DEFINED.createCheck();

    assertSame(USER_DEFINED, check.constraint());
    assertEquals(1, check.input().size());
    var expiry = check.input().stream().findFirst().get();

    assertThrows(
      NumberFormatException.class,
      () -> expiry.set("invalid"));
  }

  @Test
  public void createCheck_whenUserDefinedDurationAndInputOutOfRange_thenCheckSucceeds() throws Exception {
    var check = USER_DEFINED.createCheck();

    assertSame(USER_DEFINED, check.constraint());
    assertEquals(1, check.input().size());
    var expiry = check.input().stream().findFirst().get();

    assertEquals(ExpiryConstraint.NAME, expiry.name());
    assertEquals(Duration.class, expiry.type());
    assertNull(expiry.get());

    expiry.set(String.valueOf(USER_DEFINED.maxDuration().toMinutes() + 1));

    assertFalse(check.execute());
  }

  @Test
  public void createCheck_whenUserDefinedDurationAndInputInRange_thenCheckSucceeds() throws Exception {
    var check = USER_DEFINED.createCheck();

    assertSame(USER_DEFINED, check.constraint());
    assertEquals(1, check.input().size());
    var expiry = check.input().stream().findFirst().get();

    assertEquals(ExpiryConstraint.NAME, expiry.name());
    assertEquals(Duration.class, expiry.type());
    assertNull(expiry.get());

    expiry.set("0");
    expiry.set(String.valueOf(USER_DEFINED.minDuration().toMinutes()));

    assertTrue(check.execute());
  }
}
