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

package com.google.solutions.jitaccess.core.adapters;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudasset.v1.CloudAsset;
import com.google.api.services.cloudasset.v1.model.IamPolicyAnalysis;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.solutions.jitaccess.core.AccessDeniedException;
import com.google.solutions.jitaccess.core.AccessException;
import com.google.solutions.jitaccess.core.ApplicationVersion;
import com.google.solutions.jitaccess.core.NotAuthenticatedException;

import javax.enterprise.context.RequestScoped;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/** Adapter for the Asset Inventory API. */
@RequestScoped
public class AssetInventoryAdapter {
  public static final String OAUTH_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
  private static final int ANALYZE_IAM_POLICY_TIMEOUT_SECS = 30;

  private final GoogleCredentials credentials;

  public AssetInventoryAdapter(GoogleCredentials credentials) throws IOException {
    Preconditions.checkNotNull(credentials, "credentials");

    this.credentials = credentials;
  }

  private CloudAsset createService() throws IOException
  {
    try {
      return new CloudAsset
        .Builder(
          GoogleNetHttpTransport.newTrustedTransport(),
          new GsonFactory(),
          new HttpCredentialsAdapter(GoogleCredentials.getApplicationDefault()))
        .setApplicationName(ApplicationVersion.USER_AGENT)
        .build();
    }
    catch (GeneralSecurityException e) {
      throw new IOException("Creating a CloudAsset client failed", e);
    }
  }

  /**
   * Find resources accessible by a user: - resources the user has been directly granted access to -
   * resources which the user has inherited access to - resources which the user can access because
   * of a group membership
   *
   * NB. For group membership resolution to work, the service account must have the right
   * privileges in Cloud Identity/Workspace.
   */
  public IamPolicyAnalysis analyzeResourcesAccessibleByUser(
      String scope,
      UserId user,
      boolean expandResources)
      throws AccessException, IOException {
    Preconditions.checkNotNull(scope, "scope");
    Preconditions.checkNotNull(user, "user");

    assert (scope.startsWith("organizations/")
        || scope.startsWith("folders/")
        || scope.startsWith("projects/"));

    try {
      return createService().v1()
        .analyzeIamPolicy(scope)
        .setAnalysisQueryIdentitySelectorIdentity("user:" + user.getEmail())
        .setAnalysisQueryOptionsExpandResources(expandResources)
        .setAnalysisQueryConditionContextAccessTime(
          DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
        .setExecutionTimeout(String.format("%ds", ANALYZE_IAM_POLICY_TIMEOUT_SECS))
        .execute()
        .getMainAnalysis();
    } catch (GoogleJsonResponseException e) { // TODO Catch 403
      throw new NotAuthenticatedException("Not authenticated", e);
    } catch (HttpResponseException e) { // TODO: Catch 404
      throw new AccessDeniedException(String.format("Denied access to scope '%s': %s", scope, e.getMessage()), e);
    }
  }
}
