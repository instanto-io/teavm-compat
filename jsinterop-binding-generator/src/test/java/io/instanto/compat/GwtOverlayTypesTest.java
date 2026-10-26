package io.instanto.compat;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

public class GwtOverlayTypesTest {
  @Test
  public void bridgesOverlayFactoriesArgumentsAndNullsAcrossFiles() {
    String overlay =
        "package sample; import com.google.gwt.core.client.JavaScriptObject; "
            + "public final class Range extends JavaScriptObject { protected Range() {} "
            + "public static native Range create() /*-{return {start:1};}-*/; "
            + "public final int start(){return read(this);} "
            + "private static native int read(Range value) /*-{return value.start;}-*/;}";
    String consumer =
        "package other; import sample.Range; class Use {"
            + "static native Range echo(Range value) /*-{return value;}-*/;}";
    var pipeline = new BindingPipeline(List.of(overlay, consumer), true);
    String result = pipeline.transform(overlay);
    assertTrue(result.contains("super(value)"));
    assertTrue(result.contains("value == null ? null : new Range(value)"));
    assertTrue(result.contains("sample.Range.$wrapNative("));
    assertTrue(result.contains("value == null ? null : value.unwrap()"));
    String other = pipeline.transform(consumer);
    assertTrue(other.contains("sample.Range.$wrapNative("));
    assertTrue(other.contains("value == null ? null : value.unwrap()"));
    assertFalse(overlay.contains("org.teavm"));
  }

  @Test
  public void rejectsUnsupportedStatefulConstructors() {
    String source =
        "import com.google.gwt.core.client.JavaScriptObject; "
            + "class Bad extends JavaScriptObject { Bad() { int x = 1; } }";
    var pipeline = new BindingPipeline(List.of(source), false);
    assertThrows(IllegalArgumentException.class, () -> pipeline.transform(source));
  }
}
