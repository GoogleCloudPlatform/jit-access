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

import com.google.solutions.jitaccess.web.RequestContext;
import com.google.solutions.jitaccess.web.RequireDebugMode;
import com.google.solutions.jitaccess.web.RequireIapPrincipal;
import com.google.solutions.jitaccess.web.RuntimeEnvironment;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API controller for diagnostics.
 */
@Dependent
@Path("/diagnostics")
@RequireIapPrincipal
@RequireDebugMode
public class DiagnosticsResource {

  @Inject
  RequestContext requestContext;

  /**
   * Check if the application is ready to receive requests.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("whoami")
  public @NotNull DiagnosticsResource.SubjectInfo whoami() {
    return new SubjectInfo(
      requestContext.user().email,
      requestContext.subject().principals()
        .stream()
        .map(p -> new PrincipalInfo(p.id().type(), p.id().value()))
        .collect(Collectors.toList()));
  }

  public record SubjectInfo(
    @NotNull String email,
    @NotNull List<PrincipalInfo> principals
  ) {}

  public record PrincipalInfo(
    @NotNull String type,
    @NotNull String email
  ) {}
}
