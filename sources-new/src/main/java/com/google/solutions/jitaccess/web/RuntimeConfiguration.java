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

import com.google.solutions.jitaccess.apis.clients.CloudIdentityGroupsClient;
import com.google.solutions.jitaccess.apis.clients.IamCredentialsClient;
import com.google.solutions.jitaccess.apis.clients.ResourceManagerClient;
import com.google.solutions.jitaccess.apis.clients.SecretManagerClient;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

class RuntimeConfiguration {
  private final @NotNull Function<String, String> readSetting;

  /**
   * Cloud Identity/Workspace customer ID.
   */
  final @NotNull Setting<String> customerId;

  /**
   * Topic (within the resource hierarchy) that binding information will
   * publish to.
   */
  final @NotNull Setting<String> notificationTopicName;


  /**
   * Zone to apply to dates when sending notifications.
   */
  final @NotNull Setting<ZoneId> notificationTimeZone;

  /**
   * CEL expression for mapping userIDs to email addresses.
   */
  final @NotNull Setting<String> smtpAddressMapping;

  /**
   * SMTP server for sending notifications.
   */
  final @NotNull Setting<String> smtpHost;

  /**
   * SMTP port for sending notifications.
   */
  final @NotNull Setting<Integer> smtpPort;

  /**
   * Enable StartTLS.
   */
  final @NotNull Setting<Boolean> smtpEnableStartTls;

  /**
   * Human-readable sender name used for notifications.
   */
  final @NotNull Setting<String> smtpSenderName;

  /**
   * Email address used for notifications.
   */
  final @NotNull Setting<String> smtpSenderAddress;

  /**
   * SMTP username.
   */
  final @NotNull Setting<String> smtpUsername;

  /**
   * SMTP password. For Gmail, this should be an application-specific password.
   */
  final @NotNull Setting<String> smtpPassword;

  /**
   * Path to a SecretManager secret that contains the SMTP password.
   * For Gmail, this should be an application-specific password.
   *
   * The path must be in the format projects/x/secrets/y/versions/z.
   */
  final @NotNull Setting<String> smtpSecret;

  /**
   * Extra JavaMail options.
   */
  final @NotNull Setting<String> smtpExtraOptions;

  /**
   * Backend Service Id for token validation
   */
  final @NotNull Setting<String> backendServiceId;

  /**
   * Connect timeout for HTTP requests to backends.
   */
  final @NotNull Setting<Duration> backendConnectTimeout;

  /**
   * Read timeout for HTTP requests to backends.
   */
  final @NotNull Setting<Duration> backendReadTimeout;

  /**
   * Write timeout for HTTP requests to backends.
   */
  final @NotNull Setting<Duration> backendWriteTimeout;

  public RuntimeConfiguration(@NotNull Map<String, String> settings) {
    this(key -> settings.get(key));
  }

  public RuntimeConfiguration(Function<String, String> readSetting) {
    this.readSetting = readSetting;

    this.customerId = new StringSetting(
     "RESOURCE_CUSTOMER_ID",
      null);

    //
    // Backend service id (Cloud Run only).
    //
    this.backendServiceId = new StringSetting("IAP_BACKEND_SERVICE_ID", null);

    //
    // Notification settings.
    //
    this.notificationTimeZone = new ZoneIdSetting("NOTIFICATION_TIMEZONE");
    this.notificationTopicName = new StringSetting("NOTIFICATION_TOPIC", null);

    //
    // SMTP settings.
    //
    this.smtpAddressMapping = new StringSetting("SMTP_ADDRESS_MAPPING", "");
    this.smtpHost = new StringSetting("SMTP_HOST", "smtp.gmail.com");
    this.smtpPort = new IntSetting("SMTP_PORT", 587);
    this.smtpEnableStartTls = new BooleanSetting("SMTP_ENABLE_STARTTLS", true);
    this.smtpSenderName = new StringSetting("SMTP_SENDER_NAME", "JIT Access");
    this.smtpSenderAddress = new StringSetting("SMTP_SENDER_ADDRESS", null);
    this.smtpUsername = new StringSetting("SMTP_USERNAME", null);
    this.smtpPassword = new StringSetting("SMTP_PASSWORD", null);
    this.smtpSecret = new StringSetting("SMTP_SECRET", null);
    this.smtpExtraOptions = new StringSetting("SMTP_OPTIONS", null);

    //
    // Backend settings.
    //
    this.backendConnectTimeout = new DurationSetting(
     "BACKEND_CONNECT_TIMEOUT",
      ChronoUnit.SECONDS,
      Duration.ofSeconds(5));
    this.backendReadTimeout = new DurationSetting(
     "BACKEND_READ_TIMEOUT",
      ChronoUnit.SECONDS,
      Duration.ofSeconds(20));
    this.backendWriteTimeout = new DurationSetting(
     "BACKEND_WRITE_TIMEOUT",
      ChronoUnit.SECONDS,
      Duration.ofSeconds(5));
  }

  boolean isSmtpConfigured() {
    var requiredSettings = List.of(smtpHost, smtpPort, smtpSenderName, smtpSenderAddress);
    return requiredSettings.stream().allMatch(s -> s.isValid());
  }

  @NotNull Set<String> getRequiredOauthScopes() {
    return new HashSet<>(List.of(
      ResourceManagerClient.OAUTH_SCOPE,
      IamCredentialsClient.OAUTH_SCOPE,
      SecretManagerClient.OAUTH_SCOPE,
      CloudIdentityGroupsClient.OAUTH_SCOPE));
  }

  // -------------------------------------------------------------------------
  // Inner classes.
  // -------------------------------------------------------------------------

  public abstract class Setting<T> {
    private final String key;
    private final T defaultValue;

    protected abstract T parse(String value);

    protected Setting(String key, T defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }

    public T getValue() {
      var value = readSetting.apply(key);
      if (value != null) {
        value = value.trim();
        if (!value.isEmpty()) {
          return parse(value);
        }
      }

      if (this.defaultValue != null) {
        return this.defaultValue;
      }
      else {
        throw new IllegalStateException("No value provided for " + this.key);
      }
    }

    public boolean isValid() {
      try {
        getValue();
        return true;
      }
      catch (Exception ignored) {
        return false;
      }
    }
  }

  private class StringSetting extends Setting<String> {
    public StringSetting(String key, String defaultValue) {
      super(key, defaultValue);
    }

    @Override
    protected String parse(String value) {
      return value;
    }
  }

  private class IntSetting extends Setting<Integer> {
    public IntSetting(String key, Integer defaultValue) {
      super(key, defaultValue);
    }

    @Override
    protected @NotNull Integer parse(@NotNull String value) {
      return Integer.parseInt(value);
    }
  }

  private class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String key, Boolean defaultValue) {
      super(key, defaultValue);
    }

    @Override
    protected @NotNull Boolean parse(String value) {
      return Boolean.parseBoolean(value);
    }
  }

  private class DurationSetting extends Setting<Duration> {
    private final ChronoUnit unit;
    public DurationSetting(String key, ChronoUnit unit, Duration defaultValue) {
      super(key, defaultValue);
      this.unit = unit;
    }

    @Override
    protected Duration parse(@NotNull String value) {
      return Duration.of(Integer.parseInt(value), this.unit);
    }
  }

  private class ZoneIdSetting extends Setting<ZoneId> {
    public ZoneIdSetting(String key) {
      super(key, ZoneOffset.UTC);
    }

    @Override
    protected @NotNull ZoneId parse(@NotNull String value) {
      return ZoneId.of(value);
    }
  }

  private class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> enumClass;

    public EnumSetting(
      Class<E> enumClass,
      String key,
      E defaultValue
    ) {
      super(key, defaultValue);
      this.enumClass = enumClass;
    }

    @Override
    protected @NotNull E parse(@NotNull String value) {
      return E.valueOf(this.enumClass, value.trim().toUpperCase());
    }
  }
}
