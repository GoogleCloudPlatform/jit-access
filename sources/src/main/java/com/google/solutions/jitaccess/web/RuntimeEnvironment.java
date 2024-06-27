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

package com.google.solutions.jitaccess.web;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.GenericData;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.solutions.jitaccess.ApplicationVersion;
import com.google.solutions.jitaccess.catalog.Catalog;
import com.google.solutions.jitaccess.catalog.auth.GroupMapping;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.auth.UserId;
import com.google.solutions.jitaccess.apis.clients.CloudIdentityGroupsClient;
import com.google.solutions.jitaccess.apis.clients.Diagnosable;
import com.google.solutions.jitaccess.apis.clients.DiagnosticsResult;
import com.google.solutions.jitaccess.apis.clients.HttpTransport;
import com.google.solutions.jitaccess.catalog.policy.*;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides access to runtime configuration (AppEngine, local). To be injected using CDI.
 */
@Singleton
public class RuntimeEnvironment {
  private static final String CONFIG_IMPERSONATE_SA = "jitaccess.impersonateServiceAccount";
  private static final String CONFIG_DEBUG_MODE = "jitaccess.debug";
  private static final String CONFIG_PROJECT = "jitaccess.project";

  private final String projectId;
  private final String projectNumber;
  private final @NotNull UserId applicationPrincipal;
  private final GoogleCredentials applicationCredentials;

  /**
   * Configuration, based on app.yaml environment variables.
   */
  private final RuntimeConfiguration configuration = new RuntimeConfiguration(System::getenv);

  // -------------------------------------------------------------------------
  // Private helpers.
  // -------------------------------------------------------------------------

  private static HttpResponse getMetadata() throws IOException {
    var genericUrl = new GenericUrl(
      ComputeEngineCredentials.getMetadataServerUrl() +
        "/computeMetadata/v1/project/?recursive=true");

    var request = new NetHttpTransport()
      .createRequestFactory()
      .buildGetRequest(genericUrl);

    request.setParser(new JsonObjectParser(GsonFactory.getDefaultInstance()));
    request.getHeaders().set("Metadata-Flavor", "Google");
    request.setThrowExceptionOnExecuteError(true);

    try {
      return request.execute();
    }
    catch (UnknownHostException exception) {
      throw new IOException(
        "Cannot find the metadata server. This is likely because code is not running on Google Cloud.",
        exception);
    }
  }

  public boolean isRunningOnAppEngine() {
    return System.getenv().containsKey("GAE_SERVICE");
  }

  public boolean isRunningOnCloudRun() {
    return System.getenv().containsKey("K_SERVICE");
  }

  public String getBackendServiceId() {
    throw new RuntimeException("NIY");
  }

  // -------------------------------------------------------------------------
  // Public methods.
  // -------------------------------------------------------------------------

  public RuntimeEnvironment() {
    //
    // Create a log adapter. We can't rely on injection as the adapter
    // is request-scoped.
    //
    var logger = new JsonLogger(System.out);

    if (!this.configuration.isSmtpConfigured()) {
      logger.warn(
        LogEvents.RUNTIME_STARTUP,
        "The SMTP configuration is incomplete");
    }

    if (isRunningOnAppEngine() || isRunningOnCloudRun()) {
      //
      // Initialize using service account attached to AppEngine or Cloud Run.
      //
      try {
        GenericData projectMetadata =
          getMetadata().parseAs(GenericData.class);

        this.projectId = (String) projectMetadata.get("projectId");
        this.projectNumber = projectMetadata.get("numericProjectId").toString();

        var defaultCredentials = (ComputeEngineCredentials)GoogleCredentials.getApplicationDefault();
        this.applicationPrincipal = new UserId(defaultCredentials.getAccount());

        if (defaultCredentials.getScopes().containsAll(this.configuration.getRequiredOauthScopes())) {
          //
          // Default credential has all the right scopes, use it as-is.
          //
          this.applicationCredentials = defaultCredentials;
        }
        else {
          //
          // Extend the set of scopes to include required non-cloud APIs by
          // letting the service account impersonate itself.
          //
          this.applicationCredentials = ImpersonatedCredentials.create(
            defaultCredentials,
            this.applicationPrincipal.email,
            null,
            this.configuration.getRequiredOauthScopes().stream().toList(),
            0);
        }

        logger.info(
          LogEvents.RUNTIME_STARTUP,
          String.format("Running in project %s (%s) as %s, version %s",
            this.projectId,
            this.projectNumber,
            this.applicationPrincipal,
            ApplicationVersion.VERSION_STRING));
      }
      catch (IOException e) {
        logger.error(
          LogEvents.RUNTIME_STARTUP,
          "Failed to lookup instance metadata", e);
        throw new RuntimeException("Failed to initialize runtime environment", e);
      }
    }
    else if (isDebugModeEnabled()) {
      //
      // Initialize using development settings and credential.
      //
      this.projectId = System.getProperty(CONFIG_PROJECT, "dev");
      this.projectNumber = "0";

      try {
        var defaultCredentials = GoogleCredentials.getApplicationDefault();

        var impersonateServiceAccount = System.getProperty(CONFIG_IMPERSONATE_SA);
        if (impersonateServiceAccount != null && !impersonateServiceAccount.isEmpty()) {
          //
          // Use the application default credentials (ADC) to impersonate a
          // service account. This step is necessary to ensure we have a
          // credential for the right set of scopes, and that we're not running
          // with end-user credentials.
          //
          this.applicationCredentials = ImpersonatedCredentials.create(
            defaultCredentials,
            impersonateServiceAccount,
            null,
            this.configuration.getRequiredOauthScopes().stream().toList(),
            0);

          //
          // If we lack impersonation permissions, ImpersonatedCredentials
          // will keep retrying until the call timeout expires. The effect
          // is that the application seems hung.
          //
          // To prevent this from happening, force a refresh here. If the
          // refresh fails, fail application startup.
          //
          this.applicationCredentials.refresh();
          this.applicationPrincipal = new UserId(impersonateServiceAccount);
        }
        else if (defaultCredentials instanceof ServiceAccountCredentials) {
          //
          // Use ADC as-is.
          //
          this.applicationCredentials = defaultCredentials;
          this.applicationPrincipal = new UserId(
              ((ServiceAccountCredentials) this.applicationCredentials).getServiceAccountUser());
        }
        else {
          throw new RuntimeException(String.format(
            "You're using user credentials as application default "
              + "credentials (ADC). Use -D%s=<service-account-email> to impersonate "
              + "a service account during development",
            CONFIG_IMPERSONATE_SA));
        }
      }
      catch (IOException e) {
        throw new RuntimeException("Failed to lookup application credentials", e);
      }

      logger.warn(
        LogEvents.RUNTIME_STARTUP,
        String.format("Running in development mode as %s", this.applicationPrincipal));
    }
    else {
      throw new RuntimeException(
        "Application is not running on AppEngine or Cloud Run, and debug mode is disabled. Aborting startup");
    }
  }

  public boolean isDebugModeEnabled() {
    return Boolean.getBoolean(CONFIG_DEBUG_MODE);
  }

  public UriBuilder createAbsoluteUriBuilder(@NotNull UriInfo uriInfo) {
    return uriInfo
      .getBaseUriBuilder()
      .scheme(isRunningOnAppEngine() || isRunningOnCloudRun() ? "https" : "http");
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectNumber() {
    return projectNumber;
  }

  //---------------------------------------------------------------------------
  // Producers.
  //---------------------------------------------------------------------------

  @Produces
  @Singleton
  public @NotNull Diagnosable produceDevModeDiagnosable() {
    final String name = "DevModeIsDisabled";
    return new Diagnosable() {
      @Override
      public Collection<DiagnosticsResult> diagnose() {
        if (!isDebugModeEnabled()) {
          return List.of(new DiagnosticsResult(name));
        }
        else {
          return List.of(
            new DiagnosticsResult(
              name,
              false,
              "Application is running in development mode"));
        }
      }
    };
  }

  @Produces
  public @NotNull CloudIdentityGroupsClient.Options produceCloudIdentityGroupsClientOptions() {
    return new CloudIdentityGroupsClient.Options(
      this.configuration.customerId.getValue());
  }

  @Produces
  public GoogleCredentials produceApplicationCredentials() {
    return applicationCredentials;
  }

  @Produces
  public @NotNull HttpTransport.Options produceHttpTransportOptions() {
    return new HttpTransport.Options(
      this.configuration.backendConnectTimeout.getValue(),
      this.configuration.backendReadTimeout.getValue(),
      this.configuration.backendWriteTimeout.getValue());
  }

  @Produces
  public @NotNull RequestContextLogger produceLogger(RequestContext context) {
    return new RequestContextLogger(context);
  }

  @Produces
  public @NotNull Subject produceSubject(RequestContext context) {
    return context.subject();
  }

  @Produces
  public @NotNull Catalog produceCatalog(@NotNull Subject subject) {
    // TODO: load YAML
    var environment = new EnvironmentPolicy("test", "Test policy");
    var system = new SystemPolicy(environment, "test-system", "Test policy");
    new JitGroupPolicy(
      system,
      "test-group",
      "Test group with custom expiry",
      new AccessControlList(List.of(
        new AccessControlList.AllowedEntry(new UserId("alice@c.joonix.net"), -1))),
      Map.of(
        Policy.ConstraintClass.JOIN,
        List.of(
          new CelConstraint(
            "justification",
            "You must provide a justification",
            List.of(new CelConstraint.StringVariable("justification", "Justification", 1, 100)),
            "input.justification.matches('^b/[0-9]+$')"),
          new ExpiryConstraint(Duration.ofMinutes(1), Duration.ofDays(1)))));
    new JitGroupPolicy(
      system,
      "test-group-fixed",
      "Test group with fixed expiry",
      new AccessControlList(List.of(
        new AccessControlList.AllowedEntry(new UserId("alice@c.joonix.net"), -1))),
      Map.of(
        Policy.ConstraintClass.JOIN,
        List.of(new ExpiryConstraint(Duration.ofDays(1).plusMinutes(1)))));

    return new Catalog(
      subject,
      Map.of(environment.name(), environment));
  }

  @Produces
  @Singleton
  public @NotNull GroupMapping produceGroupMapping() {
    return new GroupMapping(this.configuration.groupsDomain.getValue());
  }
}
