package com.google.solutions.jitaccess.catalog.policy;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class TestAbstractProperty {
  //---------------------------------------------------------------------------
  // Properties.
  //---------------------------------------------------------------------------

  @Test
  public void basicProperties() {
    var property = new AbstractProperty<String>(String.class, "name", "display name") {

      @Override
      protected String convertFromString(@Nullable String value) {
        throw new IllegalStateException();
      }

      @Override
      protected String convertToString(@Nullable String value) {
        throw new IllegalStateException();
      }

      @Override
      protected void setCore(@Nullable String value) {
        throw new IllegalStateException();
      }

      @Override
      protected @Nullable String getCore() {
        throw new IllegalStateException();
      }
    };

    assertEquals("name", property.name());
    assertEquals("display name", property.displayName());
    assertEquals(String.class, property.type());
  }

  //---------------------------------------------------------------------------
  // Duration.
  //---------------------------------------------------------------------------

  private class DurationProperty extends AbstractDurationProperty {
    private @Nullable Duration value;

    public DurationProperty(Duration min, Duration max) {
      super("name", "display name", min, max);
    }

    @Override
    protected void setCore(@Nullable Duration value) {
      this.value = value;
    }

    @Override
    protected @Nullable Duration getCore() {
      return this.value;
    }
  }

  @Test
  public void duration_whenInvalid_thenSetThrowsException() {
    var property = new DurationProperty(null, null);

    assertThrows(
      NullPointerException.class,
      () -> property.set(null));
    assertThrows(
      IllegalArgumentException.class,
      () -> property.set(""));
    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("four"));
  }

  @Test
  public void duration_whenOutOfRange_thenSetThrowsException() {
    var property = new DurationProperty(Duration.ofSeconds(1), Duration.ofSeconds(3));

    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("0"));
    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("4"));
  }

  @Test
  public void duration_whenInRange_thenSetSucceeds() {
    var property = new DurationProperty(Duration.ofSeconds(1), Duration.ofSeconds(3));

    property.set(" 1  ");
    assertEquals("1", property.get());

    property.set("3");
    assertEquals("3", property.get());
  }

  @Test
  public void duration_whenRangeUnbounded_thenSetSucceeds() {
    var property = new DurationProperty(null, null);

    property.set(" -1  ");
    assertEquals("-1", property.get());

    property.set("30000000");
    assertEquals("30000000", property.get());
  }
  
  //---------------------------------------------------------------------------
  // Integer.
  //---------------------------------------------------------------------------

  private class IntegerProperty extends AbstractIntegerProperty {
    private @Nullable Integer value;

    public IntegerProperty(Integer min, Integer max) {
      super("name", "display name", min, max);
    }

    @Override
    protected void setCore(@Nullable Integer value) {
      this.value = value;
    }

    @Override
    protected @Nullable Integer getCore() {
      return this.value;
    }
  }

  @Test
  public void integer_whenInvalid_thenSetThrowsException() {
    var property = new IntegerProperty(null, null);

    assertThrows(
      NullPointerException.class,
      () -> property.set(null));
    assertThrows(
      IllegalArgumentException.class,
      () -> property.set(""));
    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("four"));
  }

  @Test
  public void integer_whenOutOfRange_thenSetThrowsException() {
    var property = new IntegerProperty(1, 3);

    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("0"));
    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("4"));
  }

  @Test
  public void integer_whenInRange_thenSetSucceeds() {
    var property = new IntegerProperty(1, 3);

    property.set(" 1  ");
    assertEquals("1", property.get());

    property.set("3");
    assertEquals("3", property.get());
  }

  @Test
  public void integer_whenRangeUnbounded_thenSetSucceeds() {
    var property = new IntegerProperty(null, null);

    property.set(String.valueOf(Integer.MIN_VALUE));
    assertEquals(String.valueOf(Integer.MIN_VALUE), property.get());

    property.set(String.valueOf(Integer.MAX_VALUE));
    assertEquals(String.valueOf(Integer.MAX_VALUE), property.get());
  }

  //---------------------------------------------------------------------------
  // Boolean.
  //---------------------------------------------------------------------------

  private class BooleanProperty extends AbstractBooleanProperty {
    private @Nullable Boolean value;

    public BooleanProperty() {
      super("name", "display name");
    }

    @Override
    protected void setCore(@Nullable Boolean value) {
      this.value = value;
    }

    @Override
    protected @Nullable Boolean getCore() {
      return this.value;
    }
  }


  @Test
  public void boolean_whenInvalid_thenSetThrowsException() {
    var property = new BooleanProperty();

    assertThrows(
      NullPointerException.class,
      () -> property.set(null));
  }

  @Test
  public void boolean_whenInRange_thenSetSucceeds() {
    var property = new BooleanProperty();

    property.set(" TRUE");
    assertEquals("true", property.get());

    property.set(" False");
    assertEquals("false", property.get());

    property.set(" ");
    assertEquals("false", property.get());
  }

  //---------------------------------------------------------------------------
  // String.
  //---------------------------------------------------------------------------

  private class StringProperty extends AbstractStringProperty {
    private @Nullable String value;

    public StringProperty() {
      super("name", "display name", 2, 5);
    }

    @Override
    protected void setCore(@Nullable String value) {
      this.value = value;
    }

    @Override
    protected @Nullable String getCore() {
      return this.value;
    }
  }

  @Test
  public void string_whenInvalid_thenSetThrowsException() {
    var property = new StringProperty();

    assertThrows(
      NullPointerException.class,
      () -> property.set(null));
  }

  @Test
  public void string_whenOutOfRange_thenSetThrowsException() {
    var property = new StringProperty();

    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("a"));
    assertThrows(
      IllegalArgumentException.class,
      () -> property.set("aaaaaa"));
  }

  @Test
  public void string_whenInRange_thenSetSucceeds() {
    var property = new StringProperty();

    property.set(" test     ");
    assertEquals("test", property.get());
  }
}
