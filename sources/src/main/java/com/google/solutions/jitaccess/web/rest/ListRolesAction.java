package com.google.solutions.jitaccess.web.rest;

import com.google.common.base.Preconditions;
import com.google.solutions.jitaccess.core.AccessDeniedException;
import com.google.solutions.jitaccess.core.AccessException;
import com.google.solutions.jitaccess.core.Exceptions;
import com.google.solutions.jitaccess.core.RoleBinding;
import com.google.solutions.jitaccess.core.catalog.ActivationType;
import com.google.solutions.jitaccess.core.catalog.Entitlement;
import com.google.solutions.jitaccess.core.catalog.ProjectId;
import com.google.solutions.jitaccess.core.catalog.project.MpaProjectRoleCatalog;
import com.google.solutions.jitaccess.web.LogAdapter;
import com.google.solutions.jitaccess.web.LogEvents;
import com.google.solutions.jitaccess.web.iap.IapPrincipal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * List roles (within a project) that the user can activate.
 */
public class ListRolesAction extends AbstractAction {
  private final @NotNull MpaProjectRoleCatalog catalog;

  public ListRolesAction(
    @NotNull LogAdapter logAdapter,
    @NotNull MpaProjectRoleCatalog catalog
  ) {
    super(logAdapter);
    this.catalog = catalog;
  }

  public @NotNull ResponseEntity execute(
    @NotNull IapPrincipal iapPrincipal,
    @Nullable String projectIdString
  ) throws AccessException {
    Preconditions.checkArgument(
      projectIdString != null && !projectIdString.trim().isEmpty(),
      "A projectId is required");

    var userContext = this.catalog.createContext(iapPrincipal.email());
    var projectId = new ProjectId(projectIdString);

    try {
      var entitlements = this.catalog.listEntitlements(userContext, projectId);

      return new ResponseEntity(
        entitlements.available()
          .stream()
          .map(ent -> new ResponseEntity.Item(
            ent.id().roleBinding(),
            ent.activationType(),
            ent.status(),
            ent.validity() != null ? ent.validity().end().getEpochSecond() : null))
          .collect(Collectors.toList()),
        entitlements.warnings());
    }
    catch (Exception e) {
      this.logAdapter
        .newErrorEntry(
          LogEvents.API_LIST_ROLES,
          String.format("Listing project roles failed: %s", Exceptions.getFullMessage(e)))
        .addLabels(le -> addLabels(le, e))
        .addLabels(le -> addLabels(le, projectId))
        .write();

      throw new AccessDeniedException("Listing project roles failed, see logs for details");
    }
  }

  public static class ResponseEntity {
    public final Set<String> warnings;
    public final @NotNull List<Item> roles;

    private ResponseEntity(
      @NotNull List<Item> roles,
      Set<String> warnings
    ) {
      Preconditions.checkNotNull(roles, "roles");

      this.warnings = warnings;
      this.roles = roles;
    }

    public static class Item {
      public final @NotNull RoleBinding roleBinding;
      public final ActivationType activationType;
      public final Entitlement.Status status;
      public final Long /* optional */ validUntil;

      public Item(
        @NotNull RoleBinding roleBinding,
        ActivationType activationType,
        Entitlement.Status status,
        Long validUntil) {

        Preconditions.checkNotNull(roleBinding, "roleBinding");

        this.roleBinding = roleBinding;
        this.activationType = activationType;
        this.status = status;
        this.validUntil = validUntil;
      }
    }
  }
}
