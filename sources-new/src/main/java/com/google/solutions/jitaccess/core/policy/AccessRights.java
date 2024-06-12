package com.google.solutions.jitaccess.core.policy;

/**
 * Right, or combination of rights on a role. Used in ACLs.
 */
public enum AccessRights {
  /**
   * Request to activate a role.
   */
  REQUEST(AccessRights.REQUEST_MASK),

  /**
   * Approve activation requests from other users.
   */
  APPROVE_OTHERS(AccessRights.APPROVE_OTHERS_MASK),

  /**
   * Self-approve activation requests.
   */
  APPROVE_SELF(AccessRights.REQUEST_MASK | AccessRights.APPROVE_SELF_MASK),

  /**
   * List users that a role applies to.
   */
  LIST_USERS(AccessRights.LIST_USERS_MASK);

  // + additional rights for background tasks (granted to SA)

  /**
   * Bit field.
   */
  private final int mask;

  AccessRights(int mask) {
    this.mask = mask;
  }

  private static final int REQUEST_MASK = 1;
  private static final int APPROVE_OTHERS_MASK = 2;
  private static final int APPROVE_SELF_MASK = 4;
  private static final int LIST_USERS_MASK = 8;

  /**
   * @return bit field representation.
   */
  int mask() {
    return this.mask;
  }
}
