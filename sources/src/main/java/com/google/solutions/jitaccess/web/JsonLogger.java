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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.solutions.jitaccess.catalog.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic logger implementation that writes JSON-structured output.
 */
public class JsonLogger implements Logger {
  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  protected final @NotNull Appendable output;

  JsonLogger(@NotNull Appendable output) {
    this.output = output;
  }

  /**
   * Emit the log entry to the log.
   */
  private void log(LogEntry entry) {
    try {
      this.output.append(JSON_MAPPER.writeValueAsString(entry)).append("\n");
    }
    catch (IOException e) {
      try {
        this.output.append(String.format("Failed to log: %s\n", entry.message));
      }
      catch (IOException ignored) {
      }
    }
  }

  protected @NotNull Map<String, String> createLabels(String eventId) {
    var labels = new HashMap<String, String>();
    labels.put("event", eventId);
    return labels;
  }

  protected @Nullable String traceId() {
    return null;
  }

  //---------------------------------------------------------------------------
  // Logger.
  //---------------------------------------------------------------------------

  @Override
  public void info(
    @NotNull String eventId,
    @NotNull String message
  ) {
    log(new LogEntry(
      "INFO",
      message,
      createLabels(eventId),
      traceId()));
  }

  @Override
  public void warn(
    @NotNull String eventId,
    @NotNull String message
  ) {
    log(new LogEntry(
      "WARN",
      message,
      createLabels(eventId),
      traceId()));
  }

  @Override
  public void error(
    @NotNull String eventId,
    @NotNull String message
  ) {
    log(new LogEntry(
      "ERROR",
      message,
      createLabels(eventId),
      traceId()));
  }

  @Override
  public void error(
    @NotNull String eventId,
    @NotNull String message,
    @NotNull Exception exception
  ) {
    log(new LogEntry(
      "ERROR",
      String.format("%s: %s", message, exception.getMessage()),
      createLabels(eventId),
      traceId()));
  }

  //---------------------------------------------------------------------
  // Inner classes.
  //---------------------------------------------------------------------

  /**
   * Entry that, when serialized to JSON, can be parsed and interpreted by Cloud Logging.
   */
  public static class LogEntry {
    @JsonProperty("severity")
    private final @NotNull String severity;

    @JsonProperty("message")
    private final @NotNull String message;

    @JsonProperty("logging.googleapis.com/labels")
    private final @NotNull Map<String, String> labels;

    @JsonProperty("logging.googleapis.com/trace")
    private final @Nullable String traceId;

    private LogEntry(
      @NotNull String severity,
      @NotNull String message,
      @NotNull Map<String, String> labels,
      @Nullable String traceId
    ) {
      this.severity = severity;
      this.message = message;
      this.traceId = traceId;
      this.labels = labels;
    }
  }
}
