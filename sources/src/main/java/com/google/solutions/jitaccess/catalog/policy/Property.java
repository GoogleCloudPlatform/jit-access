package com.google.solutions.jitaccess.catalog.policy;

public interface Property {
  /**
   * @return display name of the property.
   */
  String displayName();

  /**
   * @return name of the property.
   */
  String name();

  /**
   * Type of the property.
   */
  Class<?> type();

  /**
   * Parse string value and assign to property.
   */
  void set(String s);

  /**
   * @return typed value.
   */
  Object value();
}
