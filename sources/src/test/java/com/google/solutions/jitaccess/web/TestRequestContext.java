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

package com.google.solutions.jitaccess.web;

import com.google.solutions.jitaccess.auth.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TestRequestContext {
  private static final EndUserId SAMPLE_USER = new EndUserId("user-1@example.com");
  private static final GroupId SAMPLE_GROUP = new GroupId("group-1@example.com");
  private static final Directory SAMPLE_DIRECTORY = new Directory("example.com");

  @Test
  public void whenNotAuthenticated() {
    var resolver = Mockito.mock(SubjectResolver.class);
    var context = new RequestContext(resolver);

    assertFalse(context.isAuthenticated());

    assertEquals("anonymous", context.subject().user().email);
    assertEquals(1, context.subject().principals().size());
    assertEquals(
      context.subject().user(),
      context.subject().principals().stream().findFirst().get().id());

    assertEquals(IapDevice.UNKNOWN.deviceId(), context.device().deviceId());
  }

  @Test
  public void whenAuthenticated() throws Exception {
    var resolver = Mockito.mock(SubjectResolver.class);
    when(resolver.resolvePrincipals(eq(SAMPLE_USER), eq(SAMPLE_DIRECTORY)))
      .thenReturn(Set.of(
        new Principal(SAMPLE_USER),
        new Principal(SAMPLE_GROUP)));

    var context = new RequestContext(resolver);
    context.authenticate(SAMPLE_USER, SAMPLE_DIRECTORY, new IapDevice("device-1", List.of()));

    assertEquals(SAMPLE_USER.email, context.subject().user().email);
    assertEquals(2, context.subject().principals().size());
    assertEquals(2, context.subject().principals().size()); // Invoke again to trigger cache

    assertEquals("device-1", context.device().deviceId());

    verify(resolver, times(1)).resolvePrincipals(eq(SAMPLE_USER), eq(SAMPLE_DIRECTORY));
  }
}
