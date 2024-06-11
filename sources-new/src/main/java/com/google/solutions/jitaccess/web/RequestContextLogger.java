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

import jakarta.enterprise.context.RequestScoped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Logger that augments log messages using information
 * from the current request context.
 */
@RequestScoped
public class RequestContextLogger extends JsonLogger {
  private final @NotNull RequestContext requestContext;
  private @Nullable String traceId;

  RequestContextLogger(
    @NotNull Appendable output,
    @NotNull RequestContext requestContext
  ) {
    super(output);
    this.requestContext = requestContext;
  }

  RequestContextLogger(@NotNull RequestContext requestContext) {
    this(System.out, requestContext);
  }

  /**
   * Set Trace ID for current request.
   */
  void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  @Override
  protected @Nullable String traceId() {
    return this.traceId();
  }

  @Override
  protected Map<String, String> createLabels(String eventId) {
    var labels = super.createLabels(eventId);

    if (this.requestContext.isAuthenticated()) {
      labels.put("user_id", requestContext.user().email);
      labels.put("device_id", requestContext.device().deviceId());
      labels.put("device_access_levels",
        String.join(", ", requestContext.device().accessLevels()));
    }

    return labels;
  }
}
