package com.google.solutions.jitaccess.catalog.policy;

import java.util.Optional;

public interface Policy {
  Optional<Policy> parent();
  Optional<AccessControlList> accessControlList();
}
