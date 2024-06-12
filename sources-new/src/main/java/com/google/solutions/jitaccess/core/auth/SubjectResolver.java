package com.google.solutions.jitaccess.core.auth;

import com.google.solutions.jitaccess.apis.clients.AccessException;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Resolves information necessary to build a subject from a user ID.
 */
@Singleton
public class SubjectResolver {
  /**
   * Build a subject for a given user. The subject includes all the user's
   * principals, including:
   *
   * - the user's ID
   * - roles
   * - groups
   *
   */
  public Subject resolve(
    @NotNull UserId user
  ) throws AccessException, IOException {
    throw new RuntimeException("NIY");
  }
}
