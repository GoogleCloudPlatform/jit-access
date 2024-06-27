package com.google.solutions.jitaccess.web;

import com.google.solutions.jitaccess.catalog.auth.*;
import com.google.solutions.jitaccess.apis.clients.AccessException;
import jakarta.enterprise.context.RequestScoped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;

@RequestScoped
public class RequestContext {
  /**
   * Pseudo-subject for unauthenticated users.
   */
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

  private final @NotNull AuthenticationContext authenticationContext;
  private final @NotNull SubjectResolver subjectResolver;

  public RequestContext(@NotNull SubjectResolver subjectResolver) {
    this.authenticationContext = new AuthenticationContext();
    this.subjectResolver = subjectResolver;
  }

  /**
   * Authenticate the request context.
   */
  void authenticate(UserId userId, Device device) {
    if (isAuthenticated()) {
      throw new IllegalStateException(
        "Request context has been authenticated before");
    }

    this.authenticationContext.subject = new Subject() {
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
            try {
              this.cachedPrincipals = subjectResolver.resolve(this.user()).principals();
            }
            catch (AccessException | IOException e) {
              throw new RuntimeException(
                String.format("Resolving principals for user %s failed", this.user()),
                e);
            }
          }

          return this.cachedPrincipals;
        }
      }
    };
    this.authenticationContext.device = device;
  }

  boolean isAuthenticated() {
    return this.authenticationContext.subject != ANONYMOUS_SUBJECT;
  }

  public @NotNull Device device() {
    return this.authenticationContext.device;
  }

  public @NotNull Subject subject() {
    return this.authenticationContext;
  }

  public @NotNull UserId user() {
    return this.authenticationContext.user();
  }

  private static class AuthenticationContext implements Subject {
    private Subject subject = ANONYMOUS_SUBJECT;
    private @NotNull Device device = IapDevice.UNKNOWN;


    @Override
    public @NotNull UserId user() {
      return this.subject.user();
    }

    @Override
    public @NotNull Set<Principal> principals() {
      return this.subject.principals();
    }
  }
}
