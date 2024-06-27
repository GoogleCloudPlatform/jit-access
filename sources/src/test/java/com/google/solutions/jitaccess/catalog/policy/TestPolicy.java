package com.google.solutions.jitaccess.catalog.policy;

import com.google.solutions.jitaccess.catalog.auth.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class TestPolicy {
  private static final UserId SAMPLE_USER = new UserId("user@example.com");

  private static final JitGroupId SAMPLE_GROUPID = new JitGroupId("env-1", "system-1", "group-1");

  private static Subject createSubject(
    UserId user,
    Set<PrincipalId> otherPrincipals
  ) {
    var subject = Mockito.mock(Subject.class);
    when(subject.user()).thenReturn(user);
    when(subject.principals()).thenReturn(
      Stream.concat(otherPrincipals.stream(), Stream.<PrincipalId>of(user))
        .map(p -> new Principal(p))
        .collect(Collectors.toSet()));

    return subject;
  }

  private class SamplePolicy implements  Policy {
    @Override
    public @NotNull Collection<Constraint> constraints(ConstraintClass action) {
      return List.of();
    }

    @Override
    public @NotNull Optional<Policy> parent() {
      return Optional.empty();
    }

    @Override
    public @NotNull Optional<AccessControlList> accessControlList() {
      return Optional.empty();
    }
  }

  //---------------------------------------------------------------------------
  // checkAccess.
  //---------------------------------------------------------------------------

  @Test
  public void checkAccess_whenPolicyHasNoAcl_ThenAccessAllowedByAcl() {
    var policy = new SamplePolicy();

    assertTrue(policy.checkAccess(
      createSubject(SAMPLE_USER, Set.of()),
      EnumSet.of(PolicyRight.JOIN)));
  }

  @Test
  public void checkAccess_whenPolicyHasEmptyAcl() {
    var policy = new SamplePolicy() {
      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of()));
      }
    };

    assertFalse(policy.checkAccess(
      createSubject(SAMPLE_USER, Set.of()),
      EnumSet.of(PolicyRight.JOIN)));
  }

  @Test
  public void checkAccess_whenParentHasNoAcl() {
    var parentPolicy = new SamplePolicy() {
      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.empty();
      }
    };

    var policy = new SamplePolicy() {
      @Override
      public @NotNull Optional<Policy> parent() {
        return Optional.of(parentPolicy);
      }

      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of(
          new AccessControlList.AllowedEntry(SAMPLE_USER, -1)
        )));
      }
    };

    assertTrue(policy.checkAccess(
      createSubject(SAMPLE_USER, Set.of()),
      EnumSet.of(PolicyRight.JOIN)));
  }

  @Test
  public void checkAccess_whenParentDeniesAccess() {
    var parentPolicy = new SamplePolicy() {
      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of(
          new AccessControlList.DeniedEntry(SAMPLE_USER, -1)
        )));
      }
    };

    var policy = new SamplePolicy() {
      @Override
      public @NotNull Optional<Policy> parent() {
        return Optional.of(parentPolicy);
      }

      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of(
          new AccessControlList.AllowedEntry(SAMPLE_USER, -1)
        )));
      }
    };

    assertFalse(policy.checkAccess(
      createSubject(SAMPLE_USER, Set.of()),
      EnumSet.of(PolicyRight.JOIN)));
  }

  @Test
  public void checkAccess_whenChildDeniesAccess() {
    var parentPolicy = new SamplePolicy() {
      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of(
          new AccessControlList.AllowedEntry(SAMPLE_USER, -1)
        )));
      }
    };

    var policy = new SamplePolicy() {
      @Override
      public @NotNull Optional<Policy> parent() {
        return Optional.of(parentPolicy);
      }

      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of(
          new AccessControlList.DeniedEntry(SAMPLE_USER, -1)
        )));
      }
    };

    assertFalse(policy.checkAccess(
      createSubject(SAMPLE_USER, Set.of()),
      EnumSet.of(PolicyRight.JOIN)));
  }

  @Test
  public void checkAccess_whenParentAndChildGrantAccess() {
    var parentPolicy = new SamplePolicy() {
      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of(
          new AccessControlList.AllowedEntry(SAMPLE_USER, -1)
        )));
      }
    };

    var policy = new SamplePolicy() {
      @Override
      public @NotNull Optional<Policy> parent() {
        return Optional.of(parentPolicy);
      }

      @Override
      public @NotNull Optional<AccessControlList> accessControlList() {
        return Optional.of(new AccessControlList(List.of(
          new AccessControlList.AllowedEntry(SAMPLE_USER, PolicyRight.JOIN.toMask())
        )));
      }
    };

    assertTrue(policy.checkAccess(
      createSubject(SAMPLE_USER, Set.of()),
      EnumSet.of(PolicyRight.JOIN)));
  }
}
