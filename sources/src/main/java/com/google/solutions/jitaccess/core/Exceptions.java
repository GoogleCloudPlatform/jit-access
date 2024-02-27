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

package com.google.solutions.jitaccess.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Exceptions {
  private Exceptions() {
  }

  public static @NotNull String getFullMessage(@Nullable Throwable e) {
    var buffer = new StringBuilder();

    for (; e != null; e = e.getCause()) {
      if (buffer.length() > 0) {
        buffer.append(", caused by ");
        buffer.append(e.getClass().getSimpleName());

        if (e.getMessage() != null) {
          buffer.append(": ");
          buffer.append(e.getMessage());
        }
      } else {
        buffer.append(e.getMessage());
      }
    }

    return buffer.toString();
  }
}
