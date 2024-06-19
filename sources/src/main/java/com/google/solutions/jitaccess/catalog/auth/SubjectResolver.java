package com.google.solutions.jitaccess.catalog.auth;

import com.google.solutions.jitaccess.apis.clients.AccessException;
import com.google.solutions.jitaccess.apis.clients.CloudIdentityGroupsClient;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Resolves information necessary to build a subject from a user ID.
 */
@Singleton
public class SubjectResolver {
  private final @NotNull CloudIdentityGroupsClient groupsClient;
  private final @NotNull GroupMapping groupMapping;
  private final @NotNull Executor executor;

  public SubjectResolver(
    @NotNull CloudIdentityGroupsClient groupsClient,
    @NotNull GroupMapping groupMapping,
    @NotNull Executor executor
  ) {
    this.groupsClient = groupsClient;
    this.groupMapping = groupMapping;
    this.executor = executor;
  }

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
