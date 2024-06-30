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
  private final boolean isRequired;

  protected final @Nullable T minInclusive;
  protected final @Nullable T maxInclusive;

  protected AbstractProperty(
    @NotNull Class<T> type,
    @NotNull String name,
    @NotNull String displayName,
    boolean isRequired,
    @Nullable T minInclusive,
    @Nullable T maxInclusive
  ) {
    this.type = type;
    this.name = name;
    this.displayName = displayName;
    this.isRequired = isRequired;
    this.minInclusive = minInclusive;
    this.maxInclusive = maxInclusive;
  }

  protected AbstractProperty(
    @NotNull Class<T> type,
    @NotNull String name,
    @NotNull String displayName,
    boolean isRequired
  ) {
    this(type, name, displayName, isRequired, null, null);
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

  @Override
  public boolean isRequired() {
    return isRequired;
  }

  /**
   * Convert string representation to the typed representation.
   *
   * @throws IllegalArgumentException when a conversion is not possible.
   */
  protected abstract @Nullable T convertFromString(@Nullable String value);

  /**
   * Convert to string representation.
   *
   * @throws IllegalArgumentException when a conversion is not possible.
   */
  protected abstract @Nullable String convertToString(@Nullable T value);

  /**
   * Validate the value.
   * @throws IllegalArgumentException if the value is invalid.
   */
  protected void validateRange(@Nullable T value) {
    Preconditions.checkArgument(
      !this.isRequired || value != null,
      String.format("%s must be assigned a value", this.name()));
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
  public @NotNull Class<?> type() {
    return this.type;
  }

  @Override
  public @NotNull Optional<String> minInclusive() {
    return this.minInclusive != null
      ? Optional.of(convertToString(this.minInclusive))
      : Optional.empty();
  }

  @Override
  public @NotNull Optional<String> maxInclusive() {
    return this.maxInclusive != null
      ? Optional.of(convertToString(this.maxInclusive))
      : Optional.empty();
  }

  @Override
  public final void set(@Nullable String s) {
    var value = convertFromString(s);
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
    boolean isRequired,
    @Nullable Duration minInclusive,
    @Nullable Duration maxInclusive)
  {
    super(Duration.class, name, displayName, isRequired, minInclusive, maxInclusive);
  }

  @Override
  protected void validateRange(@Nullable Duration value) {
    super.validateRange(value);

    if (value != null && this.minInclusive != null && value.compareTo(this.minInclusive) < 0) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too small", this.name()));
    }

    if (value != null && this.maxInclusive != null && value.compareTo(this.maxInclusive) > 0) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too large", this.name()));
    }
  }

  @Override
  protected @Nullable Duration convertFromString(@Nullable String value) {
    return value != null ? Duration.ofSeconds(Integer.parseInt(value.trim())) : null;
  }

  @Override
  protected @Nullable String convertToString(@Nullable Duration value) {
    return value != null ? String.valueOf(value.toSeconds()) : null;
  }
}

/**
 * An int-typed property.
 */
abstract class AbstractIntegerProperty extends AbstractProperty<Integer> {
  protected AbstractIntegerProperty(
    @NotNull String name,
    @NotNull String displayName,
    boolean isRequired,
    @Nullable Integer minInclusive,
    @Nullable Integer maxInclusive)
  {
    super(int.class, name, displayName, isRequired, minInclusive, maxInclusive);
  }

  @Override
  protected void validateRange(@Nullable Integer value) {
    super.validateRange(value);

    if (value != null && this.minInclusive != null && value < this.minInclusive) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too small", this.name()));
    }

    if (value != null && this.maxInclusive != null && value > this.maxInclusive) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too large", this.name()));
    }
  }

  @Override
  protected @Nullable Integer convertFromString(@Nullable String value) {
    return value != null ? Integer.parseInt(value.trim()) : null;
  }

  @Override
  protected @Nullable String convertToString(@Nullable Integer value) {
    return value != null ? String.valueOf(value) : null;
  }
}

/**
 * A boolean-typed property.
 */
abstract class AbstractBooleanProperty extends AbstractProperty<Boolean> {
  protected AbstractBooleanProperty(
    @NotNull String name,
    @NotNull String displayName,
    boolean isRequired)
  {
    super(Boolean.class, name, displayName, isRequired);
  }

  @Override
  protected @Nullable Boolean convertFromString(@Nullable String value) {
    return value != null ? Boolean.parseBoolean(value.trim()) : null;
  }

  @Override
  protected @Nullable String convertToString(@Nullable Boolean value) {
    return value != null ? value.toString() : null;
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
    boolean isRequired,
    @NotNull int minLength,
    @NotNull int maxLength)
  {
    super(
      String.class,
      name,
      displayName,
      isRequired,
      String.valueOf(minLength),
      String.valueOf(maxLength));

    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  @Override
  protected void validateRange(@Nullable String value) {
    super.validateRange(value);

    if (value != null && value.length() < this.minLength) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too short", this.name()));
    }

    if (value != null && value.length() > this.maxLength) {
      throw new IllegalArgumentException(
        String.format("The value for %s is too long", this.name()));
    }
  }

  @Override
  protected @Nullable String convertFromString(@Nullable String value) {
    return value != null ? value.trim() : null;
  }

  @Override
  protected @Nullable String convertToString(@Nullable String value) {
    return value;
  }
}