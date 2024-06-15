package com.google.solutions.jitaccess.core.access;

import com.google.solutions.jitaccess.core.auth.JitGroupId;
import com.google.solutions.jitaccess.core.auth.Subject;
import com.google.solutions.jitaccess.core.policy.AccessRights;

public interface Request {
  Subject subject();

  JitGroupId group();
  AccessRights requiredRights();

  //+ user input
}