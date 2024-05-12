//
// Copyright 2022 Google LLC
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

package com.google.solutions.jitaccess.core.util;

import com.google.solutions.jitaccess.core.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExceptions {
  @Test
  public void getFullMessageConcatenatesCauses()
  {
    var exception = new AccessDeniedException(
      "Access denied",
      new IllegalStateException(
        "Illegal state",
        new NullPointerException()));

    assertEquals(
      "Access denied, caused by IllegalStateException: Illegal state, caused by NullPointerException",
      Exceptions.getFullMessage(exception));
  }
}
