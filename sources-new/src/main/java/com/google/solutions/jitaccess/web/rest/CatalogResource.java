package com.google.solutions.jitaccess.web.rest;

import com.google.solutions.jitaccess.web.RequireIapPrincipal;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * REST API controller for catalog.
 */
@Dependent
@Path("/catalog")
@RequireIapPrincipal
public class CatalogResource {
  @Inject
  ListGroups listGroups;

  @Inject
  ListEnvironments listEnvironments;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments")
  public @NotNull ListEnvironments.Response listEnvironments() {
    return this.listEnvironments.execute();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments/{environment}/groups")
  public @NotNull ListGroups.Response listGroups(
    @PathParam("environment") @Nullable String environment
  ) {
    return this.listGroups.execute(environment);
  }
}
