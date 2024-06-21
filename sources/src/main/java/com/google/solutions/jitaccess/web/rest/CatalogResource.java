package com.google.solutions.jitaccess.web.rest;

import com.google.solutions.jitaccess.catalog.Catalog;
import com.google.solutions.jitaccess.web.RequireIapPrincipal;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@Dependent
@Path("/catalog")
@RequireIapPrincipal
public class CatalogResource {//TODO: test
  @Inject
  Catalog catalog;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("environments")
  public @NotNull CatalogResource.EnvironmentsInfo listEnvironments() {
    var environments = this.catalog.environments()
      .stream()
      .map(env -> new EnvironmentInfo(env.name(), env.description()))
      .collect(Collectors.toList());

    return new EnvironmentsInfo(environments);
  }


  public record EnvironmentsInfo(
    @NotNull List<EnvironmentInfo> environments
  ) {
  }

  public record EnvironmentInfo(
    @NotNull String name,
    @NotNull String description
  ) {}
}
