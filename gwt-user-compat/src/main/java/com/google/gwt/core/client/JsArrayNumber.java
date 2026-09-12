package com.google.gwt.core.client;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/** Typed wrapper for a JavaScript array. */
public class JsArrayNumber extends JavaScriptObject {
  public JsArrayNumber(JSObject value) {
    super(value);
  }

  public static JsArrayNumber of(JSObject value) {
    return value == null ? null : new JsArrayNumber(value);
  }

  public int length() {
    return lengthOf(unwrap());
  }

  public double get(int index) {
    return getAt(unwrap(), index);
  }

  public void set(int index, double value) {
    setAt(unwrap(), index, value);
  }

  public void push(double value) {
    pushTo(unwrap(), value);
  }

  @JSBody(params = "a", script = "return a.length;")
  private static native int lengthOf(JSObject a);

  @JSBody(
      params = {"a", "i"},
      script = "return a[i];")
  private static native double getAt(JSObject a, int i);

  @JSBody(
      params = {"a", "i", "v"},
      script = "a[i]=v;")
  private static native void setAt(JSObject a, int i, double v);

  @JSBody(
      params = {"a", "v"},
      script = "a.push(v);")
  private static native void pushTo(JSObject a, double v);
}
