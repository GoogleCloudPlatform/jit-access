//
// Copyright 2021 Google LLC
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

package com.google.solutions.jitaccess.common;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

/**
 * Utility class for Exceptions.
 */
public class Exceptions {
  private Exceptions() {}

  /**
   * Create a message that includes details for all nested
   * exceptions.
   */
  public static @NotNull String fullMessage(@Nullable Throwable e) {
    return fullMessage(e, true);
  }

  /**
   * Create a message that includes details for all nested
   * exceptions.
   */
  public static @NotNull String fullMessage(
    @Nullable Throwable e,
    boolean includeNestedExceptionDetails
  ) {
    var buffer = new StringBuilder();

    for (; e != null; e = e.getCause()) {
      if (!buffer.isEmpty()) {
        if (includeNestedExceptionDetails) {
          buffer.append(", caused by ");
          buffer.append(e.getClass().getSimpleName());
        }

        if (e.getMessage() != null) {
          buffer.append(": ");
        }
      }

      if (e.getMessage() != null) {
        buffer.append(e.getMessage());
      }

      //
      // Include details about suppressed exceptions, if any.
      //
      if (e.getSuppressed() != null && e.getSuppressed().length > 0 && includeNestedExceptionDetails) {
        boolean first = true;
        buffer.append(" (also: ");
        for (var suppressed : e.getSuppressed()) {
          if (first) {
            first = false;
          }
          else {
            buffer.append(", ");
          }

          buffer.append(suppressed.getClass().getSimpleName());
          if (suppressed.getMessage() != null) {
            buffer.append(": ");
            buffer.append(suppressed.getMessage());
          }
        }
        buffer.append(")");
      }
    }

    return buffer.toString();
  }

  /**
   * Remove common wrapper exception to reveal the actual exception.
   */
  public static @NotNull Exception unwrap(@NotNull Exception e) {
    while ((e instanceof UncheckedExecutionException || e instanceof ExecutionException) &&
      e.getCause() != null &&
      e.getCause() instanceof Exception) {
      e = (Exception)e.getCause();
    }

    return e;
  }

  /**
   * Get the innermost cause of an exception.
   */
  public static @NotNull Exception rootCause(@NotNull Exception e) {
    if (e.getCause() instanceof Exception cause) {
      return rootCause(cause);
    }
    else {
      return e;
    }
  }

  /**
   * Return an exception's back trace as string.
   */
  public static @NotNull String stackTrace(@NotNull Exception e) {
    try (var sw = new StringWriter()) {
      e.printStackTrace(new PrintWriter(sw));
      return sw.toString();
    }
    catch (IOException ignored) {
      return "(unable to capture stack trace)";
    }
  }
}
