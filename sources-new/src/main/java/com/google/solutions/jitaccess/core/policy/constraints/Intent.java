package com.google.solutions.jitaccess.core.policy.constraints;

import com.google.solutions.jitaccess.core.policy.AccessRights;

public enum Intent {
  REQUEST,
  APPROVE,
  EVALUATE;
  // + Administer?

  AccessRights requiredRights;
}
