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
import com.google.solutions.jitaccess.core.Logger;
import jakarta.enterprise.context.RequestScoped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Basic logger implementation that writes JSON-structured
 * output to STDOUT.
 */
public class ConsoleLogger implements Logger {
  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  protected final @NotNull Appendable output;
  private @Nullable String traceId;

  ConsoleLogger(@NotNull Appendable output) {
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

  protected Map<String, String> createLabels(String eventId) {
    var labels = new HashMap<String, String>();
    labels.put("event", eventId);
    return labels;
  }

  /**
   * Set Trace ID for current request.
   */
  public void setTraceId(String traceId) {
    this.traceId = traceId;
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
      eventId,
      message,
      createLabels(eventId),
      this.traceId));
  }

  @Override
  public void warn(
    @NotNull String eventId,
    @NotNull String message
  ) {
    log(new LogEntry(
      "WARN",
      eventId,
      message,
      createLabels(eventId),
      this.traceId));
  }

  @Override
  public void error(
    @NotNull String eventId,
    @NotNull String message
  ) {
    log(new LogEntry(
      "ERROR",
      eventId,
      message,
      createLabels(eventId),
      this.traceId));
  }

  @Override
  public void error(
    @NotNull String eventId,
    @NotNull String message,
    @NotNull Exception exception
  ) {
    log(new LogEntry(
      "ERROR",
      eventId,
      String.format("%s: %s", message, exception.getMessage()),
      createLabels(eventId),
      this.traceId));
  }

  //---------------------------------------------------------------------
  // Inner classes.
  //---------------------------------------------------------------------

  /**
   * Entry that, when serialized to JSON, can be parsed and interpreted by Cloud Logging.
   */
  public class LogEntry {
    @JsonProperty("severity")
    private final String severity;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("logging.googleapis.com/labels")
    private final @NotNull Map<String, String> labels;

    @JsonProperty("logging.googleapis.com/trace")
    private final String traceId;

    private LogEntry(
      String severity,
      String eventId,
      String message,
      Map<String, String> labels,
      String traceId
    ) {
      this.severity = severity;
      this.message = message;
      this.traceId = traceId;
      this.labels = labels;
    }

    public @NotNull RequestContextLogger.LogEntry addLabel(String label, String value) {
      assert !this.labels.containsKey(label);

      this.labels.put(label, value);
      return this;
    }

    public RequestContextLogger.LogEntry addLabels(@NotNull Function<RequestContextLogger.LogEntry, RequestContextLogger.LogEntry> func) {
      return func.apply(this);
    }
  }
}
