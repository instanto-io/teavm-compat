package com.google.gwt.core.client;

import java.util.function.Function;
import org.teavm.jso.*;

/** Typed array view. The factory preserves overlay narrowing at the native boundary. */
public class JsArray<T extends JavaScriptObject> extends JavaScriptObject {
  private final Function<JSObject, T> factory;

  public JsArray(JSObject value, Function<JSObject, T> factory) {
    super(value);
    this.factory = factory;
  }

  public int length() {
    return length(unwrap());
  }

  public T get(int index) {
    return factory.apply(item(unwrap(), index));
  }

  @JSBody(params = "a", script = "return a.length;")
  private static native int length(JSObject a);

  @JSBody(
      params = {"a", "i"},
      script = "return a[i];")
  private static native JSObject item(JSObject a, int i);
}
