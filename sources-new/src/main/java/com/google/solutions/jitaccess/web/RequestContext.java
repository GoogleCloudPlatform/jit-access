package com.google.solutions.jitaccess.web;

import com.google.solutions.jitaccess.core.auth.*;
import jakarta.enterprise.context.RequestScoped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@RequestScoped
public class RequestContext implements Subject {
  private final @NotNull UserId ANONYMOUS_USER = new UserId("anonymous");

  /**
   * Current user ID, if authenticated.
   */
  private @Nullable UserId user;

  /**
   * Current user's device, if authenticated.
   */
  private @Nullable Device device;

  /**
   * Cached set of user principals, looked up lazily.
   */
  private @Nullable Set<Principal> cachedPrincipals;
  private @NotNull Object cachedPrincipalsLock = new Object();

  public RequestContext() {
    this.user = ANONYMOUS_USER;
    this.device = IapDevice.UNKNOWN;
  }

  /**
   * Authenticate the request context using the IAP principal.
   * @param principal
   */
  void authenticate(UserId user, Device device) {
    if (isAuthenticated()) {
      throw new IllegalStateException(
        "Request context has been authenticated before");
    }
    this.user = user;
    this.device = device;
  }

  boolean isAuthenticated() {
    return this.user != ANONYMOUS_USER;
  }

  public Device device() {
    return this.device;
  }

  @Override
  public @NotNull UserId user() {
    return this.user;
  }

  @Override
  public @NotNull Set<Principal> principals() {
    if (!isAuthenticated()) {
      return Set.of(new Principal(ANONYMOUS_USER));
    }

    synchronized (this.cachedPrincipalsLock)
    {
      if (this.cachedPrincipals == null) {
        // TODO: lookup
        throw new RuntimeException("NIY");
      }

      return this.cachedPrincipals;
    }
  }
}
