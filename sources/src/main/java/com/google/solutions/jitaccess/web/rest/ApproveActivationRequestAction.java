package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.solutions.jitaccess.core.AccessDeniedException;
import com.google.solutions.jitaccess.core.AccessException;
import com.google.solutions.jitaccess.core.Exceptions;
import com.google.solutions.jitaccess.core.catalog.Entitlement;
import com.google.solutions.jitaccess.core.catalog.MpaActivationRequest;
import com.google.solutions.jitaccess.core.catalog.ProjectId;
import com.google.solutions.jitaccess.core.catalog.TokenSigner;
import com.google.solutions.jitaccess.core.catalog.project.MpaProjectRoleCatalog;
import com.google.solutions.jitaccess.core.catalog.project.ProjectRole;
import com.google.solutions.jitaccess.core.catalog.project.ProjectRoleActivator;
import com.google.solutions.jitaccess.core.notifications.NotificationService;
import com.google.solutions.jitaccess.web.LogAdapter;
import com.google.solutions.jitaccess.web.LogEvents;
import com.google.solutions.jitaccess.web.RuntimeEnvironment;
import com.google.solutions.jitaccess.web.TokenObfuscator;
import com.google.solutions.jitaccess.web.iap.IapPrincipal;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ApproveActivationRequestAction extends AbstractActivationAction {
  private final @NotNull MpaProjectRoleCatalog catalog;
  private final @NotNull TokenSigner tokenSigner;

  public ApproveActivationRequestAction(
    @NotNull LogAdapter logAdapter,
    @NotNull RuntimeEnvironment runtimeEnvironment,
    @NotNull ProjectRoleActivator activator,
    @NotNull Instance<NotificationService> notificationServices,
    @NotNull MpaProjectRoleCatalog catalog,
    @NotNull TokenSigner tokenSigner
  ) {
    super(logAdapter, runtimeEnvironment, activator, notificationServices);
    this.catalog = catalog;
    this.tokenSigner = tokenSigner;
  }

  public @NotNull ResponseEntity execute(
    @NotNull IapPrincipal iapPrincipal,
    @Nullable String obfuscatedActivationToken,
    @NotNull UriInfo uriInfo
  ) throws AccessException {
    Preconditions.checkArgument(
      obfuscatedActivationToken != null && !obfuscatedActivationToken.trim().isEmpty(),
      "An activation token is required");

    var activationToken = TokenObfuscator.decode(obfuscatedActivationToken);
    var userContext = this.catalog.createContext(iapPrincipal.email());

    MpaActivationRequest<ProjectRole> activationRequest;
    try {
      activationRequest = this.tokenSigner.verify(
        this.activator.createTokenConverter(),
        activationToken);
    }
    catch (Exception e) {
      this.logAdapter
        .newErrorEntry(
          LogEvents.API_ACTIVATE_ROLE,
          String.format("Accessing the activation request failed: %s", Exceptions.getFullMessage(e)))
        .addLabels(le -> addLabels(le, e))
        .write();

      throw new AccessDeniedException("Accessing the activation request failed");
    }

    assert activationRequest.entitlements().size() == 1;
    var roleBinding = activationRequest
      .entitlements()
      .stream()
      .findFirst()
      .get()
      .roleBinding();

    try {
      var activation = this.activator.approve(userContext, activationRequest);

      assert activation != null;

      //
      // Notify listeners.
      //
      var projectId = ProjectId.parse(roleBinding.fullResourceName());
      for (var service : this.notificationServices) {
        service.sendNotification(new ApiResource.ActivationApprovedNotification(
          projectId,
          activation,
          iapPrincipal.email(),
          createActivationRequestUrl(uriInfo, projectId, activationToken)));
      }

      //
      // Leave an audit trail.
      //
      this.logAdapter
        .newInfoEntry(
          LogEvents.API_ACTIVATE_ROLE,
          String.format(
            "User %s approved role '%s' on '%s' for %s",
            iapPrincipal.email(),
            roleBinding.role(),
            roleBinding.fullResourceName(),
            activationRequest.requestingUser()))
        .addLabels(le -> addLabels(le, activationRequest))
        .write();

      return new ResponseEntity(
        iapPrincipal.email(),
        activationRequest,
        Entitlement.Status.ACTIVE);
    }
    catch (Exception e) {
      this.logAdapter
        .newErrorEntry(
          LogEvents.API_ACTIVATE_ROLE,
          String.format(
            "User %s failed to activate role '%s' on '%s' for %s: %s",
            iapPrincipal.email(),
            roleBinding.role(),
            roleBinding.fullResourceName(),
            activationRequest.requestingUser(),
            Exceptions.getFullMessage(e)))
        .addLabels(le -> addLabels(le, activationRequest))
        .addLabels(le -> addLabels(le, e))
        .write();

      if (e instanceof AccessDeniedException) {
        throw (AccessDeniedException)e.fillInStackTrace();
      }
      else {
        throw new AccessDeniedException("Approving the activation request failed", e);
      }
    }
  }
}
