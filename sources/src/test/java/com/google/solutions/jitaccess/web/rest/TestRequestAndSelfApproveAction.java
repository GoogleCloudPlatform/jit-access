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

package com.google.solutions.jitaccess.web.rest;

import com.google.solutions.jitaccess.core.AccessDeniedException;
import com.google.solutions.jitaccess.core.RoleBinding;
import com.google.solutions.jitaccess.core.auth.UserEmail;
import com.google.solutions.jitaccess.core.catalog.Activation;
import com.google.solutions.jitaccess.core.catalog.ActivationRequest;
import com.google.solutions.jitaccess.core.catalog.Entitlement;
import com.google.solutions.jitaccess.core.catalog.ProjectId;
import com.google.solutions.jitaccess.core.catalog.project.ProjectRole;
import com.google.solutions.jitaccess.core.catalog.project.ProjectRoleActivator;
import com.google.solutions.jitaccess.web.LogAdapter;
import com.google.solutions.jitaccess.web.RuntimeEnvironment;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class TestRequestAndSelfApproveAction {
  private static final UserEmail SAMPLE_USER = new UserEmail("user-1@example.com");
  private static final int DEFAULT_MAX_NUMBER_OF_ROLES = 3;

  @Test
  public void whenProjectIsNull_ThenActionThrowsException() throws Exception {
    var action = new RequestAndSelfApproveAction(
      new LogAdapter(),
      Mockito.mock(RuntimeEnvironment.class),
      Mockito.mock(ProjectRoleActivator.class),
      Mockito.mock(Instance.class),
      Mocks.createMpaProjectRoleCatalogMock());

    assertThrows(
      IllegalArgumentException.class,
      () -> action.execute(
        new MockIapPrincipal(SAMPLE_USER),
        " ",
        new RequestAndSelfApproveAction.RequestEntity()));
  }

  @Test
  public void whenRolesEmpty_ThenActionThrowsException() throws Exception {
    var action = new RequestAndSelfApproveAction(
      new LogAdapter(),
      Mockito.mock(RuntimeEnvironment.class),
      Mockito.mock(ProjectRoleActivator.class),
      Mockito.mock(Instance.class),
      Mocks.createMpaProjectRoleCatalogMock());

    var request = new RequestAndSelfApproveAction.RequestEntity();
    request.roles = List.of();
    assertThrows(
      IllegalArgumentException.class,
      () -> action.execute(
        new MockIapPrincipal(SAMPLE_USER),
        "project-1",
        request));
  }

  @Test
  public void whenRolesExceedsLimit_ThenActionThrowsException() throws Exception {
    var action = new RequestAndSelfApproveAction(
      new LogAdapter(),
      Mockito.mock(RuntimeEnvironment.class),
      Mockito.mock(ProjectRoleActivator.class),
      Mockito.mock(Instance.class),
      Mocks.createMpaProjectRoleCatalogMock());

    var request = new RequestAndSelfApproveAction.RequestEntity();
    request.justification = "test";
    request.roles = Stream
      .generate(() -> "roles/role-x")
      .limit(DEFAULT_MAX_NUMBER_OF_ROLES + 1)
      .collect(Collectors.toList());

    assertThrows(
      IllegalArgumentException.class,
      () -> action.execute(
        new MockIapPrincipal(SAMPLE_USER),
        "project-1",
        request));
  }

  @Test
  public void whenJustificationMissing_ThenActionThrowsException() throws Exception {
    var action = new RequestAndSelfApproveAction(
      new LogAdapter(),
      Mockito.mock(RuntimeEnvironment.class),
      Mockito.mock(ProjectRoleActivator.class),
      Mockito.mock(Instance.class),
      Mocks.createMpaProjectRoleCatalogMock());

    var request = new RequestAndSelfApproveAction.RequestEntity();
    request.roles = List.of("roles/browser");
    request.justification = "";

    assertThrows(
      IllegalArgumentException.class,
      () -> action.execute(
        new MockIapPrincipal(SAMPLE_USER),
        "project-1",
        request));
  }

  @Test
  public void whenActivatorThrowsException_ThenActionThrowsException() throws Exception {
    var activator = Mockito.mock(ProjectRoleActivator.class);
    when(activator
      .createJitRequest(any(), any(), any(), any(), any()))
      .thenCallRealMethod();
    when(activator
      .activate(
        argThat(ctx -> ctx.user().equals(SAMPLE_USER)),
        any()))
      .thenThrow(new AccessDeniedException("mock"));

    var action = new RequestAndSelfApproveAction(
      new LogAdapter(),
      Mockito.mock(RuntimeEnvironment.class),
      activator,
      Mockito.mock(Instance.class),
      Mocks.createMpaProjectRoleCatalogMock());

    var request = new RequestAndSelfApproveAction.RequestEntity();
    request.roles = List.of("roles/browser", "roles/browser");
    request.justification = "justification";
    request.activationTimeout = 5;

    assertThrows(
      AccessDeniedException.class,
      () -> action.execute(
        new MockIapPrincipal(SAMPLE_USER),
        "project-1",
        request));
  }

  @Test
  public void whenRolesContainDuplicates_ThenActionSucceedsAndIgnoresDuplicates() throws Exception {
    var roleBinding = new RoleBinding(new ProjectId("project-1"), "roles/browser");

    var activator = Mockito.mock(ProjectRoleActivator.class);
    when(activator
      .createJitRequest(any(), any(), any(), any(), any()))
      .thenCallRealMethod();
    when(activator
      .activate(
        argThat(ctx -> ctx.user().equals(SAMPLE_USER)),
        argThat(r -> r.entitlements().size() == 1)))
      .then(r -> new Activation<>((ActivationRequest<ProjectRole>) r.getArguments()[1]));

    var action = new RequestAndSelfApproveAction(
      new LogAdapter(),
      Mockito.mock(RuntimeEnvironment.class),
      activator,
      MockitoUtils.toCdiInstance(Mocks.createNotificationServiceMock(false)),
      Mocks.createMpaProjectRoleCatalogMock());

    var request = new RequestAndSelfApproveAction.RequestEntity();
    request.roles = List.of("roles/browser", "roles/browser");
    request.justification = "justification";
    request.activationTimeout = 5;

    var response = action.execute(new MockIapPrincipal(SAMPLE_USER), "project-1", request);

    assertEquals(SAMPLE_USER.email, response.beneficiary.email);
    assertEquals(0, response.reviewers.size());
    assertTrue(response.isBeneficiary);
    assertFalse(response.isReviewer);
    assertEquals("justification", response.justification);
    assertNotNull(response.items);
    assertEquals(1, response.items.size());
    assertEquals("project-1", response.items.get(0).projectId);
    assertEquals(roleBinding, response.items.get(0).roleBinding);
    assertEquals(Entitlement.Status.ACTIVE, response.items.get(0).status);
    assertNotNull(response.items.get(0).activationId);
  }
}
