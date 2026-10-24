package io.instanto.compat;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

public class ErasedCallbackArgumentsTest {
  private static final String BOX =
      """
      package sample;
      import jsinterop.annotations.*;
      @JsType(isNative=true, name="Object", namespace=JsPackage.GLOBAL)
      class Box<T> { @JsProperty public native void setCallback(T callback); }
      """;
  private static final String CALLBACK =
      """
      package sample;
      import jsinterop.annotations.JsFunction;
      @JsFunction interface Callback { Object call(Integer index, String[] labels); }
      """;

  @Test
  public void genericFieldSetterKeepsCallableTypeAndConvertsObjectResults() {
    String source =
        """
        package sample;
        class App {
          Box<Callback> box;
          void configure() { this.box.setCallback((index, labels) -> labels[index]); }
        }
        """;
    String result = new BindingPipeline(List.of(BOX, CALLBACK, source), false).transform(source);
    assertTrue(result, result.contains("((Box) this.box).setCallback($erasedCallback0("));
    assertTrue(result, result.contains("sample.Callback callback"));
    assertTrue(result, result.contains("Integer.valueOf(jsinterop.base.Js.asInt(a0))"));
    assertTrue(result, result.contains("a0 == null ? null :"));
    assertTrue(result, result.contains("String[] a1"));
    assertTrue(result, result.contains("jsinterop.base.Js.asAny(callback.call("));
    assertTrue(result, result.contains("if (callback == null)"));
    assertTrue(
        result, result.contains("NativeCallbacks.remember(callback, \"erased:sample.Callback\""));
  }

  @Test
  public void parameterShadowsFieldWithoutAccidentalCallbackRewrite() {
    String source =
        """
        package sample;
        class App {
          Box<Callback> box;
          void configure(Box<String> box) { box.setCallback("text"); }
        }
        """;
    assertEquals(
        source, new ErasedCallbackArguments(List.of(BOX, CALLBACK, source)).transform(source));
  }

  @Test
  public void siblingBlockLocalDoesNotHideCallbackField() {
    String source =
        """
        package sample;
        class App {
          Box<Callback> box;
          void configure() {
            { Box<String> box = null; box.setCallback("text"); }
            box.setCallback((index, labels) -> labels[index]);
          }
        }
        """;
    String result = new ErasedCallbackArguments(List.of(BOX, CALLBACK, source)).transform(source);
    assertTrue(result, result.contains("box.setCallback(\"text\")"));
    assertTrue(result, result.contains("((Box) box).setCallback($erasedCallback0("));
  }

  @Test
  public void inferredLambdaParameterDoesNotUseFieldType() {
    String source =
        """
        package sample;
        class App {
          Box<Callback> box;
          void configure(java.util.List<Box<String>> boxes) {
            boxes.forEach(box -> box.setCallback("text"));
          }
        }
        """;
    assertEquals(
        source, new ErasedCallbackArguments(List.of(BOX, CALLBACK, source)).transform(source));
  }

  @Test
  public void localContainersAndVoidCallbacksKeepTheirSignatures() {
    String callback =
        CALLBACK.replace("Object call(Integer index, String[] labels)", "void call(int index)");
    String source =
        """
        package sample;
        class App { void configure() { Box<Callback> box = null; box.setCallback(index -> {}); } }
        """;
    String result = new BindingPipeline(List.of(BOX, callback, source), false).transform(source);
    assertTrue(result, result.contains("void call(int a0)"));
    assertTrue(result, result.contains("(a0) -> callback.call(a0)"));
    assertFalse(result, result.contains("Js.asAny(callback.call"));
  }
}
