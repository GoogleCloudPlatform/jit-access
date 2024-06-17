package com.google.solutions.jitaccess.catalog.policy;

import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;
import com.google.solutions.jitaccess.cel.ExtractFunction;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.CelTypes;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.parser.CelStandardMacro;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Constraint that executes a CEL expression.
 */
public class CelConstraint implements Constraint { // TODO: test
  private static final CelRuntime CEL_RUNTIME =
    CelRuntimeFactory
      .standardCelRuntimeBuilder()
      .addFunctionBindings(ExtractFunction.BINDING)
      .build();

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
    Preconditions.checkArgument(
      requiredInput.values().stream().allMatch(p -> p.type().isPrimitive()),
      "Input properties must be of a primitive type");

    this.name = name;
    this.displayName = displayName;
    this.requiredInput = requiredInput;
    this.expression = expression;
  }

  @Override
  public String toString() {
    return String.format("%s [%s]", this.name, this.expression);
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

    var check = new Check();

    //
    // Copy input properties into a JSON object.
    //
    var input = check.add("input");
    for (var entry : inputProperties.entrySet()) {
      input.add(entry.getKey(), entry.getValue().value());
    }

    return check;
  }

  private class Check implements ConstraintCheck {
    private final @NotNull Map<String, GenericJson> variables = new HashMap<>();

    @Override
    public Context add(@NotNull String name) {
      var json = new GenericJson();
      this.variables.put(name, json);
      return new Context() {
        @Override
        public Context add(@NotNull String name, @NotNull Object value) {
          json.set(name, value);
          return this;
        }
      };
    }

    @Override
    public boolean execute() throws ConstraintException {
      var compiler = CelCompilerFactory.standardCelCompilerBuilder()
        .setStandardMacros(CelStandardMacro.STANDARD_MACROS)
        .addFunctionDeclarations(ExtractFunction.DECLARATION);

      for (var variable : this.variables.keySet()) {
        compiler.addVar(variable, CelTypes.createMap(CelTypes.STRING, CelTypes.ANY));
      }

      try {
        var ast = compiler.build().compile(expression).getAst();

        return (Boolean)CEL_RUNTIME
          .createProgram(ast)
          .eval(this.variables);

      } catch (CelValidationException | CelEvaluationException e) {
        throw new InvalidCelConstraintException(
          "The CEL expression is invalid",
          e);
      }
    }
  }

  class InvalidCelConstraintException extends ConstraintException {
    InvalidCelConstraintException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
