package com.google.solutions.jitaccess.web;

import com.google.solutions.jitaccess.core.model.PrincipalId;
import com.google.solutions.jitaccess.core.model.Subject;
import com.google.solutions.jitaccess.core.model.UserId;
import com.google.solutions.jitaccess.web.iap.DeviceInfo;
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
  private @Nullable DeviceInfo device;

  /**
   * Cached set of user principals, looked up lazily.
   */
  private @Nullable Set<PrincipalId> cachedPrincipals;
  private @NotNull Object cachedPrincipalsLock = new Object();

  /**
   * Authenticate the request context using the IAP principal.
   * @param principal
   */
  void authenicate(UserId user, DeviceInfo device) {
    if (this.user != null || this.device != null) {
      throw new IllegalStateException(
        "Request context has been authenticated before");
    }
    this.user = user;
    this.device = device;
  }

  boolean isAuthenticated() {
    return this.user != null;
  }

  public DeviceInfo device() {
    return this.device != null ? this.device : DeviceInfo.UNKNOWN;
  }

  @Override
  public @NotNull UserId user() {
    return this.user != null ? this.user : ANONYMOUS_USER;
  }

  @Override
  public @NotNull Set<PrincipalId> principals() {
    if (this.user == null) {
      return Set.of(ANONYMOUS_USER);
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
