package com.google.solutions.jitaccess.catalog.policy;

import java.util.List;
import java.util.Optional;

public interface Policy {
  Optional<Policy> parent();
  AccessControlList acl();
  List<Constraint> constraints(Policy action);
}
