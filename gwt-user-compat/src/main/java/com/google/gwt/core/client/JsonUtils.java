package com.google.gwt.core.client;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/** JSON serialization of wrapped JavaScript values. */
public final class JsonUtils {
  private JsonUtils() {}

  public static String stringify(JavaScriptObject value) {
    return stringifyNative(value == null ? null : value.unwrap());
  }

  @JSBody(params = "value", script = "return JSON.stringify(value);")
  private static native String stringifyNative(JSObject value);
}
