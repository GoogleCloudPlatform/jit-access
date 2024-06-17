package com.google.solutions.jitaccess.catalog.policy;

import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

class CelConstraint implements Constraint {
  private final @NotNull String name;
  private final @NotNull String displayName;
  private final @NotNull Map<String, Property> requiredInput;

  private final @NotNull String expression;

  public CelConstraint(
    @NotNull String name,
    @NotNull String displayName,
    @NotNull Map<String, Property> requiredInput,
    @NotNull String expression
  ) {
    this.name = name;
    this.displayName = displayName;
    this.requiredInput = requiredInput;
    this.expression = expression;
  }

  //---------------------------------------------------------------------------
  // Constraint.
  //---------------------------------------------------------------------------

  @Override
  public @NotNull String name() {
    return this.name;
  }
  @Override
  public @NotNull String displayName() {
    return this.displayName;
  }

  @Override
  public @NotNull Map<String, Property> requiredInput() {
    return this.requiredInput;
  }

  @Override
  public ConstraintCheck createCheck(
    @NotNull Map<String, Property> inputProperties
  ) {
    Preconditions.checkArgument(
      requiredInput.keySet().stream().allMatch(k -> inputProperties.containsKey(k)),
      "One or more required input properties are missing");

    //
    // Copy input properties into a JSON object.
    //
    var input = new GenericJson();
    for (var entry : inputProperties.entrySet()) {
      input.set(entry.getKey(), entry.getValue().value());
    }

    return new Check(input);
  }

  private class Check implements ConstraintCheck {
    private final @NotNull GenericJson context = new GenericJson();
    private final @NotNull GenericJson input;

    public Check(@NotNull GenericJson input) {
      this.input = input;
    }

    @Override
    public ConstraintCheck add(@NotNull String name, @NotNull Object value) {
      this.context.set(name, value);
      return this;
    }

    @Override
    public boolean execute() {
      //TODO: run CEL
      return false;
    }
  }
}
