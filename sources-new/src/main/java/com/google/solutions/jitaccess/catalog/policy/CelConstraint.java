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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constraint that executes a CEL expression.
 */
public class CelConstraint implements Constraint {
  private static final CelRuntime CEL_RUNTIME =
    CelRuntimeFactory
      .standardCelRuntimeBuilder()
      .addFunctionBindings(ExtractFunction.BINDING)
      .build();

  private final @NotNull String name;
  private final @NotNull String displayName;
  private final @NotNull Collection<Variable> variableDeclarations;
  private final @NotNull String expression;

  public CelConstraint(
    @NotNull String name,
    @NotNull String displayName,
    @NotNull Collection<Variable> variables,
    @NotNull String expression
  ) {
    this.name = name;
    this.displayName = displayName;
    this.variableDeclarations = variables;
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
  public Constraint.Check createCheck() {
    return new Check();
  }

  private class Check implements Constraint.Check {
    private final @NotNull Map<String, GenericJson> variables = new HashMap<>();
    private final List<Property> input;

    public Check() {
      var json = new GenericJson();
      this.variables.put("input", json);

      this.input = variableDeclarations.stream()
        .map(v -> v.bind(json))
        .toList();
    }

    @Override
    public @NotNull Constraint constraint() {
      return CelConstraint.this;
    }

    @Override
    public @NotNull List<Property> input() {
      return this.input;
    }

    @Override
    public Constraint.Context addContext(@NotNull String name) {
      var json = new GenericJson();
      this.variables.put(name, json);
      return new Context() {
        @Override
        public Context set(@NotNull String name, @NotNull Object value) {
          json.set(name, value);
          return this;
        }
      };
    }

    @Override
    public boolean execute() throws ConstraintException {
      for (var input : this.input) {
        if (input.value() == null) {
          throw new IllegalArgumentException(
            String.format("Input missing for '%s'", input.displayName()));
        }
      }

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
        throw new InvalidExpressionException(
          "The CEL expression is invalid",
          e);
      }
    }
  }

  static class InvalidExpressionException extends ConstraintException {
    InvalidExpressionException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public record Variable(
    @NotNull String name,
    @NotNull String displayName,
    @NotNull Class<?> type
  ) {
    /**
     * Pattern for variable names in CEL.
     */
    private static final String NAME_PATTERN = "[A-Za-z\\_]+";

    public Variable {
      Preconditions.checkArgument(
        name.matches(NAME_PATTERN),
        "Variable names must be alphanumeric");
      Preconditions.checkArgument(
        type == String.class || type == int.class || type == boolean.class,
        "Variable must be of type String, int, or boolean");
    }

    private Property bind(@NotNull GenericJson json) {
      return new Property() {
        @Override
        public String displayName() {
          return displayName;
        }

        @Override
        public String name() {
          return name;
        }

        @Override
        public Class<?> type() {
          return type;
        }

        @Override
        public void set(String s) {
          if (this.type() == int.class) {
            json.set(name, Integer.parseInt(s));
          }
          else if (this.type() == boolean.class) {
            json.set(name, Boolean.parseBoolean(s));
          }
          else if (this.type() == String.class) {
            json.set(name, s);
          }
        }

        @Override
        public Object value() {
          return json.get(name);
        }
      };
    }
  }
}
