package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.StaticJavaParser;
import java.util.List;
import org.junit.Test;

public class NativeCallbackBindingsTest {
  private static final String DECLARATION =
      "package example; import jsinterop.annotations.JsFunction; @JsFunction public interface"
          + " Callback<A,B> { Object invoke(A first,B second); }";

  @Test
  public void rawCallbackUsesItsNativeUpperBoundRatherThanObject() {
    String event =
        "package example; import jsinterop.annotations.*; @JsType(isNative=true) class Event {}";
    String callback =
        "package example; import jsinterop.annotations.*; @JsFunction interface Handler<T extends Event> { void call(T event); }";
    String query = "package example; class Query { native void on(Handler callback); }";
    String result = new NativeCallbackBindings(List.of(callback, query, event)).transform(query);
    assertEquals(query, result);
  }

  @Test
  public void boundedCallbackWithObjectResultRetainsQualifiedEventParameter() {
    String event =
        "package example; import jsinterop.annotations.*; @JsType(isNative=true) class Event {}";
    String callback =
        "package example; import jsinterop.annotations.*; @JsFunction interface Handler<T extends Event> { Object call(T event); }";
    String query = "package example; class Query { native void on(Handler callback); }";
    String result = new NativeCallbackBindings(List.of(callback, query, event)).transform(query);
    assertTrue(result, result.contains("(example.Event) jsinterop.base.Js.cast(a0)"));
    assertFalse(result, result.contains("(Object) jsinterop.base.Js.cast(a0)"));
  }

  @Test
  public void typedCallbacksConvertDomNodesAndBoxedNumbers() {
    String source =
        "import example.Callback; import com.google.gwt.dom.client.Element; import org.teavm.jso.*;"
            + " @JSClass(name=\"Object\") class Query implements JSObject { @JSMethod(\"each\")"
            + " public native void visit(Callback<Integer,Element> callback); }";
    String result = new NativeCallbackBindings(List.of(DECLARATION)).transform(source);
    var owner = StaticJavaParser.parse(result).getClassByName("Query").orElseThrow();
    assertFalse(owner.getMethodsByName("visit").get(0).isNative());
    assertTrue(result.contains("Integer.valueOf(jsinterop.base.Js.asInt(a0))"));
    assertTrue(result.contains("JavaScriptObject.of(a1)"));
    assertTrue(result.contains("@org.teavm.jso.JSMethod(\"each\")"));
  }

  @Test
  public void erasedHandlersUseTheSameIdentityKeyForRegistrationAndRemoval() {
    String source =
        "import example.Callback; class Query { public native void on(Callback callback); public"
            + " native void off(Callback callback); }";
    String result = new NativeCallbackBindings(List.of(DECLARATION)).transform(source);
    var owner = StaticJavaParser.parse(result).getClassByName("Query").orElseThrow();
    String on = owner.getMethodsByName("on").get(0).getBody().orElseThrow().toString();
    String off = owner.getMethodsByName("off").get(0).getBody().orElseThrow().toString();
    String key = "\"example.Callback[Object, Object]Object\"";
    assertTrue(on.contains(key));
    assertTrue(off.contains(key));
    assertTrue(result.contains("jsinterop.base.Js.cast(a0)"));
    assertTrue(result.contains("jsinterop.base.Js.asAny(callback.invoke("));
  }

  @Test
  public void pipelineActivatesCallbacksAndEvaluatesReturnedOverlayOnce() {
    String callback =
        "package example; import jsinterop.annotations.JsFunction; import"
            + " com.google.gwt.dom.client.Element; @JsFunction public interface Factory { Element"
            + " create(); }";
    String query =
        "package example; import jsinterop.annotations.*; @JsType(isNative=true,"
            + " namespace=JsPackage.GLOBAL, name=\"Object\") public class Query { public native"
            + " void register(Factory factory); }";
    String result = new BindingPipeline(List.of(callback, query), false).transform(query);
    assertTrue(result, result.contains("JavaScriptObject.unwrapNullable(factory.create())"));
    assertEquals(result, 1, result.split("factory.create\\(\\)", -1).length - 1);
  }

  @Test
  public void returnedCallbacksUnwrapJavaArgumentsBeforeCallingNativeCode() {
    String source =
        "import example.Callback; import com.google.gwt.dom.client.Element; class Query { public"
            + " static native Callback<Object,Element> debounce(Callback<Object,Element> callback);"
            + " }";
    String result = new NativeCallbackBindings(List.of(DECLARATION)).transform(source);
    assertTrue(
        result.contains(
            "nativeFunction.call(jsinterop.base.Js.asAny(a0),"
                + " com.google.gwt.core.client.JavaScriptObject.unwrapNullable(a1))"));
  }

  @Test
  public void samePackageCallbackWinsOverAnUnrelatedSimpleName() {
    String other = DECLARATION.replace("package example;", "package unrelated;");
    String source =
        "package example; class Query { public native void visit(Callback<Integer,Object>"
            + " callback); }";
    String result =
        new BindingPipeline(List.of(DECLARATION, other, source), false).transform(source);
    assertTrue(result, result.contains("example.Callback[Integer, Object]Object"));
    assertFalse(result, result.contains("unrelated.Callback["));
  }
}
