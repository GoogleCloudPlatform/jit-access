package com.google.solutions.jitaccess.web;

import com.google.solutions.jitaccess.core.auth.*;
import jakarta.enterprise.context.RequestScoped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@RequestScoped
public class RequestContext {
  private static final @NotNull Subject ANONYMOUS_SUBJECT = new Subject() {
    @Override
    public @NotNull UserId user() {
      return new UserId("anonymous");
    }

    @Override
    public @NotNull Set<Principal> principals() {
      return Set.of(new Principal(user()));
    }
  };

  /**
   * Current user's device.
   */
  private @NotNull Device device;

  /**
   * Current subject.
   */
  private @NotNull Subject subject;


  public RequestContext() {
    this.subject = ANONYMOUS_SUBJECT;
    this.device = IapDevice.UNKNOWN;
  }

  /**
   * Authenticate the request context.
   */
  void authenticate(UserId userId, Device device) {
    if (isAuthenticated()) {
      throw new IllegalStateException(
        "Request context has been authenticated before");
    }

    this.subject = new Subject() {
      private @Nullable Set<Principal> cachedPrincipals;
      private final @NotNull Object cachedPrincipalsLock = new Object();

      @Override
      public @NotNull UserId user() {
        return userId;
      }

      @Override
      public @NotNull Set<Principal> principals() {
        //
        // Resolve lazily.
        //
        synchronized (this.cachedPrincipalsLock)
        {
          if (this.cachedPrincipals == null) {
            // TODO: lookup
            throw new RuntimeException("NIY");
          }

          return this.cachedPrincipals;
        }
      }
    };
    this.device = device;
  }

  boolean isAuthenticated() {
    return this.subject != ANONYMOUS_SUBJECT;
  }

  public @NotNull Device device() {
    return this.device;
  }

  public @NotNull Subject subject() {
    return this.subject;
  }

  public @NotNull UserId user() {
    return this.subject.user();
  }
}
