package com.google.gwt.core.client;

import org.teavm.jso.*;

public class JsArrayMixed extends JavaScriptObject {
  public JsArrayMixed(JSObject value) {
    super(value);
  }

  public static JsArrayMixed of(JSObject value) {
    return new JsArrayMixed(value);
  }

  public void push(JavaScriptObject value) {
    pushNative(unwrap(), value.unwrap());
  }

  @JSBody(
      params = {"a", "v"},
      script = "a.push(v);")
  private static native void pushNative(JSObject a, JSObject v);
}
