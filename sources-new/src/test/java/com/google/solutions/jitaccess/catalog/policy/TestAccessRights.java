package com.google.solutions.jitaccess.catalog.policy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestAccessRights {
  //---------------------------------------------------------------------------
  // parse.
  //---------------------------------------------------------------------------

  @Test
  public void parseRequest() {
    assertEquals(AccessRights.REQUEST, AccessRights.parse("Request  "));
  }

  @Test
  public void parseApproveSelf() {
    assertEquals(AccessRights.APPROVE_SELF, AccessRights.parse(" approve_self  "));
  }

  @Test
  public void parseApproveOthers() {
    assertEquals(AccessRights.APPROVE_OTHERS, AccessRights.parse("APPROVE_OTHERS"));
  }

  @Test
  public void parseList() {
    assertEquals(
      AccessRights.REQUEST.mask() | AccessRights.APPROVE_SELF.mask(),
      AccessRights.parse("Request,approve_self,,  ").mask());
  }
}
