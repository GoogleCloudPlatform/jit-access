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

package com.google.solutions.jitaccess.web;

import com.google.solutions.jitaccess.core.clients.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

class RuntimeConfiguration {
  private final Function<String, String> readSetting;

  public RuntimeConfiguration(@NotNull Map<String, String> settings) {
    this(key -> settings.get(key));
  }

  public RuntimeConfiguration(Function<String, String> readSetting) {
    this.readSetting = readSetting;
  }

  public boolean isSmtpConfigured() {
    throw new RuntimeException("NIY");
  }

  public @NotNull Set<String> getRequiredOauthScopes() {
    var scopes = new HashSet<String>();

    scopes.add(ResourceManagerClient.OAUTH_SCOPE);
    scopes.add(IamCredentialsClient.OAUTH_SCOPE);
    scopes.add(SecretManagerClient.OAUTH_SCOPE);

    return scopes;
  }
}
