package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.StaticJavaParser;
import java.util.List;
import org.junit.Test;

public class NativeCallbackBindingsTest {
  private static final String DECLARATION =
      "package example; import jsinterop.annotations.JsFunction; @JsFunction public interface Callback<A,B> { Object invoke(A first,B second); }";

  @Test
  public void typedCallbacksConvertDomNodesAndBoxedNumbers() {
    String source =
        "import example.Callback; import com.google.gwt.dom.client.Element; import org.teavm.jso.*; @JSClass(name=\"Object\") class Query implements JSObject { @JSMethod(\"each\") public native void visit(Callback<Integer,Element> callback); }";
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
        "import example.Callback; class Query { public native void on(Callback callback); public native void off(Callback callback); }";
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
  public void returnedCallbacksUnwrapJavaArgumentsBeforeCallingNativeCode() {
    String source =
        "import example.Callback; import com.google.gwt.dom.client.Element; class Query { public static native Callback<Object,Element> debounce(Callback<Object,Element> callback); }";
    String result = new NativeCallbackBindings(List.of(DECLARATION)).transform(source);
    assertTrue(
        result.contains(
            "nativeFunction.call(jsinterop.base.Js.asAny(a0), (a1 == null ? null : a1.unwrap()))"));
  }
}
