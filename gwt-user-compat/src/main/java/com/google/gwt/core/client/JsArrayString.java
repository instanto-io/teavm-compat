package com.google.gwt.core.client;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/** Typed wrapper for a JavaScript array. */
public class JsArrayString extends JavaScriptObject {
  public static JsArrayString createArray() {
    return of(JavaScriptObject.createArray().unwrap());
  }

  public JsArrayString(JSObject value) {
    super(value);
  }

  public static JsArrayString of(JSObject value) {
    return value == null ? null : new JsArrayString(value);
  }

  public int length() {
    return lengthOf(unwrap());
  }

  public String get(int index) {
    return getAt(unwrap(), index);
  }

  public void set(int index, String value) {
    setAt(unwrap(), index, value);
  }

  public void push(String value) {
    pushTo(unwrap(), value);
  }

  @JSBody(params = "a", script = "return a.length;")
  private static native int lengthOf(JSObject a);

  @JSBody(
      params = {"a", "i"},
      script = "return a[i];")
  private static native String getAt(JSObject a, int i);

  @JSBody(
      params = {"a", "i", "v"},
      script = "a[i]=v;")
  private static native void setAt(JSObject a, int i, String v);

  @JSBody(
      params = {"a", "v"},
      script = "a.push(v);")
  private static native void pushTo(JSObject a, String v);
}
