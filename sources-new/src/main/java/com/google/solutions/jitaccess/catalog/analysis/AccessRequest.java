package com.google.solutions.jitaccess.catalog.analysis;

import com.google.solutions.jitaccess.catalog.auth.JitGroupId;
import com.google.solutions.jitaccess.catalog.auth.Subject;
import com.google.solutions.jitaccess.catalog.policy.AccessRights;

public abstract class AccessRequest {
  abstract Subject subject();

  abstract JitGroupId group();
  abstract AccessRights requiredRights();

  final void execute() {
    // check access, etc.
    // call executeCore
  }

  abstract void executeCore();

  //+ user input
}