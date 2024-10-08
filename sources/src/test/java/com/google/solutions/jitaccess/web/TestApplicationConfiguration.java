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

import com.google.solutions.jitaccess.apis.CustomerId;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestApplicationConfiguration {

  private Map<String, String> createMandatorySettings() {
    return Map.of(
      "PRIMARY_DOMAIN", "  example.com ",
      "ORGANIZATION_ID", "10000000000000001 ",
      "CUSTOMER_ID", " C123 ");
  }

  // -------------------------------------------------------------------------
  // Constructor.
  // -------------------------------------------------------------------------

  @Test
  public void constructor_whenCustomerIdMissing_thenThrows() {
    var data = new HashMap<>(createMandatorySettings());
    data.remove("CUSTOMER_ID");

    assertThrows(
      IllegalStateException.class,
      () -> new ApplicationConfiguration(data));
  }

  @Test
  public void constructor_whenPrimaryDomainMissing_thenThrows() {
    var data = new HashMap<>(createMandatorySettings());
    data.remove("PRIMARY_DOMAIN");

    assertThrows(
      IllegalStateException.class,
      () -> new ApplicationConfiguration(data));
  }

  @Test
  public void constructor_whenOrganizationIdMissing_thenThrows() {
    var data = new HashMap<>(createMandatorySettings());
    data.remove("ORGANIZATION_ID");

    assertThrows(
      IllegalStateException.class,
      () -> new ApplicationConfiguration(data));
  }

  @Test
  public void constructor_whenMandatorySettingsProvided() {
    var configuration = new ApplicationConfiguration(createMandatorySettings());
    assertEquals("example.com", configuration.groupsDomain.name());
    assertEquals(new CustomerId("C123"), configuration.customerId);
  }

  // -------------------------------------------------------------------------
  // groupsDomain.
  // -------------------------------------------------------------------------

  @Test
  public void groupsDomain_whenNotSpecified() {
    var configuration = new ApplicationConfiguration(createMandatorySettings());

    assertEquals(configuration.primaryDomain, configuration.groupsDomain);
  }

  @Test
  public void groupsDomain_whenSpecified() {
    var settings = new HashMap<>(createMandatorySettings());
    settings.put("GROUPS_DOMAIN", " groups.example.com ");
    var configuration = new ApplicationConfiguration(settings);

    assertEquals("groups.example.com", configuration.groupsDomain.name());
  }

  // -------------------------------------------------------------------------
  // Environments.
  // -------------------------------------------------------------------------

  @Test
  public void environments_whenEmpty() {
    var configuration = new ApplicationConfiguration(createMandatorySettings());
    assertTrue(configuration.environments.isEmpty());
  }

  @Test
  public void environments_whenProvided() {
    var settings = new HashMap<>(createMandatorySettings());
    settings.put("ENVIRONMENTS", "env-1,, , env-2 ");
    var configuration = new ApplicationConfiguration(settings);

    assertEquals(2, configuration.environments.size());
    assertTrue(configuration.environments.contains("env-1"));
    assertTrue(configuration.environments.contains("env-2"));
  }

  // -------------------------------------------------------------------------
  // SMTP.
  // -------------------------------------------------------------------------

  @Test
  public void smtp_whenEmpty() {
    var configuration = new ApplicationConfiguration(createMandatorySettings());

    assertFalse(configuration.isSmtpConfigured());
    assertFalse(configuration.isSmtpAuthenticationConfigured());

    assertFalse(configuration.smtpAddressMapping.isPresent());
    assertEquals("smtp.gmail.com", configuration.smtpHost);
    assertEquals(587, configuration.smtpPort);
    assertTrue(configuration.smtpEnableStartTls);
    assertEquals("JIT Groups", configuration.smtpSenderName);

    assertFalse(configuration.smtpSenderAddress.isPresent());
    assertFalse(configuration.smtpUsername.isPresent());
    assertFalse(configuration.smtpPassword.isPresent());
    assertFalse(configuration.smtpSecret.isPresent());
    assertFalse(configuration.smtpExtraOptions.isPresent());
    assertTrue(configuration.smtpExtraOptionsMap().isEmpty());
  }

  @Test
  public void smtp_whenProvided() {
    var settings = new HashMap<>(createMandatorySettings());
    settings.put("SMTP_ADDRESS_MAPPING", "email -> email");
    settings.put("SMTP_HOST", " localhost ");
    settings.put("SMTP_PORT", " 25 ");
    settings.put("SMTP_ENABLE_STARTTLS", " false");
    settings.put("SMTP_SENDER_NAME", "sender");
    settings.put("SMTP_SENDER_ADDRESS", "sender@localhost");
    settings.put("SMTP_USERNAME", "username");
    settings.put("SMTP_PASSWORD", "password");
    settings.put("SMTP_SECRET", "secret");
    settings.put("SMTP_OPTIONS", "a=1,  b = 2,  ");

    var configuration = new ApplicationConfiguration(settings);

    assertTrue(configuration.isSmtpConfigured());
    assertTrue(configuration.isSmtpAuthenticationConfigured());

    assertEquals("email -> email", configuration.smtpAddressMapping.get());
    assertEquals("localhost", configuration.smtpHost);
    assertEquals(25, configuration.smtpPort);
    assertFalse(configuration.smtpEnableStartTls);
    assertEquals("sender", configuration.smtpSenderName);

    assertEquals("sender@localhost", configuration.smtpSenderAddress.get());
    assertEquals("username", configuration.smtpUsername.get());
    assertEquals("password", configuration.smtpPassword.get());
    assertEquals("secret", configuration.smtpSecret.get());
    assertFalse(configuration.smtpExtraOptionsMap().isEmpty());
    assertEquals(2, configuration.smtpExtraOptionsMap().size());
    assertEquals("1", configuration.smtpExtraOptionsMap().get("a"));
    assertEquals("2", configuration.smtpExtraOptionsMap().get("b"));

  }
}
