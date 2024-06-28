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

package com.google.solutions.jitaccess.catalog.policy;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

/**
 * Base implementation of a property.
 */
abstract class AbstractProperty<T> implements Property {
  private final  @NotNull Class<T> type;
  private final @NotNull String name;
  private final @NotNull String displayName;

  protected final @Nullable T minInclusive;
  protected final @Nullable T maxInclusive;

  protected AbstractProperty(
    @NotNull Class<T> type,
    @NotNull String name,
    @NotNull String displayName,
    @Nullable T minInclusive,
    @Nullable T maxInclusive
  ) {
    this.type = type;
    this.name = name; // TODO: check syntax.
    this.displayName = displayName;
    this.minInclusive = minInclusive;
    this.maxInclusive = maxInclusive;
  }

  protected AbstractProperty(
    @NotNull Class<T> type,
    @NotNull String name,
    @NotNull String displayName
  ) {
    this(type, name, displayName, null, null);
  }

  /**
   * @return name of the property.
   */
  @Override
  public @NotNull String name() {
    return name;
  }

  /**
   * @return display name of the property.
   */
  @Override
  public @NotNull String displayName() {
    return displayName;
  }

  /**
   * Convert string representation to the typed representation.
   *
   * @throws IllegalArgumentException when a conversion is not possible.
   */
  protected abstract @NotNull T convertFromString(@Nullable String value);

  /**
   * Convert to string representation.
   *
   * @throws IllegalArgumentException when a conversion is not possible.
   */
  protected abstract @NotNull String convertToString(@Nullable T value);


  protected void validateNotNull(Object value) {
    Preconditions.checkNotNull(
      value,
      String.format("%s must be assigned a value", this.name()));
  }

  /**
   * Validate the value.
   * @throws IllegalArgumentException if the value is invalid.
   */
  protected void validateRange(@Nullable T value) {
    validateNotNull(value);
  }

  /**
   * Assign the (validated) new value.
   */
  protected abstract void setCore(@Nullable T value);

  /**
   * @return the assigned value.
   */
  protected abstract @Nullable T getCore();

  @Override
  public Class<?> type() {
    return this.type;
  }

  @Override
  public Optional<String> minInclusive() {
    return this.minInclusive != null
      ? Optional.of(convertToString(this.minInclusive))
      : Optional.empty();
  }

  @Override
  public Optional<String> maxInclusive() {
    return this.maxInclusive != null
      ? Optional.of(convertToString(this.maxInclusive))
      : Optional.empty();
  }

  @Override
  public final void set(@Nullable String s) {
    var value = convertFromString(s);
    validateNotNull(value);
    validateRange(value);
    setCore(value);
  }

  @Override
  public @Nullable String get() {
    var value = getCore();
    return value == null ? null : convertToString(value);
  }
}

/**
 * A Duration-typed property.
 *
 * NB. We use seconds instead of ISO-8601 durations
 * for easier client-side handling and parsing.
 */
abstract class AbstractDurationProperty extends AbstractProperty<Duration> {
  protected AbstractDurationProperty(
    @NotNull String name,
    @NotNull String displayName,
    @Nullable Duration minInclusive,
    @Nullable Duration maxInclusive)
  {
    super(Duration.class, name, displayName, minInclusive, maxInclusive);
  }

  @Override
  protected void validateRange(@Nullable Duration value) {
    if (this.minInclusive != null && value.compareTo(this.minInclusive) < 0) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too small", this.name()));
    }

    if (this.maxInclusive != null && value.compareTo(this.maxInclusive) > 0) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too large", this.name()));
    }
  }

  @Override
  protected Duration convertFromString(@Nullable String value) {
    validateNotNull(value);
    return Duration.ofSeconds(Integer.parseInt(value.trim()));
  }

  @Override
  protected String convertToString(@Nullable Duration value) {
    validateNotNull(value);
    return String.valueOf(value.toSeconds());
  }
}

/**
 * An int-typed property.
 */
abstract class AbstractIntegerProperty extends AbstractProperty<Integer> {
  protected AbstractIntegerProperty(
    @NotNull String name,
    @NotNull String displayName,
    @Nullable Integer minInclusive,
    @Nullable Integer maxInclusive)
  {
    super(int.class, name, displayName, minInclusive, maxInclusive);
  }

  @Override
  protected void validateRange(@Nullable Integer value) {
    if (this.minInclusive != null && value < this.minInclusive) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too small", this.name()));
    }

    if (this.maxInclusive != null && value > this.maxInclusive) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too large", this.name()));
    }
  }

  @Override
  protected Integer convertFromString(@Nullable String value) {
    validateNotNull(value);
    return Integer.parseInt(value.trim());
  }

  @Override
  protected String convertToString(@Nullable Integer value) {
    validateNotNull(value);
    return String.valueOf(value);
  }
}

/**
 * A boolean-typed property.
 */
abstract class AbstractBooleanProperty extends AbstractProperty<Boolean> {
  protected AbstractBooleanProperty(
    @NotNull String name,
    @NotNull String displayName)
  {
    super(Boolean.class, name, displayName);
  }

  @Override
  protected Boolean convertFromString(@Nullable String value) {
    validateNotNull(value);
    return Boolean.parseBoolean(value.trim());
  }

  @Override
  protected String convertToString(@Nullable Boolean value) {
    validateNotNull(value);
    return value.toString();
  }
}

/**
 * A String-typed property.
 */
abstract class AbstractStringProperty extends AbstractProperty<String> {
  private final int minLength;
  private final int maxLength;
  protected AbstractStringProperty(
    @NotNull String name,
    @NotNull String displayName,
    @NotNull int minLength,
    @NotNull int maxLength)
  {
    super(
      String.class,
      name,
      displayName,
      String.valueOf(minLength),
      String.valueOf(maxLength));

    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  @Override
  protected void validateRange(@Nullable String value) {
    if (value.length() < this.minLength) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too short", this.name()));
    }

    if (value.length() > this.maxLength) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too long", this.name()));
    }
  }

  @Override
  protected String convertFromString(@Nullable String value) {
    return value.trim();
  }

  @Override
  protected String convertToString(@Nullable String value) {
    return value;
  }
}