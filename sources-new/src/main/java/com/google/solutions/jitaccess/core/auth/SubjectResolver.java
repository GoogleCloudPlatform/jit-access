package com.google.solutions.jitaccess.core.auth;

import com.google.solutions.jitaccess.core.clients.AccessException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Resolves information necessary to build a subject
 * from a user ID.
 */
public class SubjectResolver {
  public Subject resolve(
    @NotNull UserId user
  ) throws AccessException, IOException {
    throw new RuntimeException("NIY");
  }
}
