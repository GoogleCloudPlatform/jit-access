package com.google.solutions.jitaccess.catalog.policy;

public interface Property {
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
