package io.instanto.compat.boxed;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Native JsInterop as GWT libraries declare it, with boxed numbers and booleans. The build adapts
 * this source with the binding generator before the TeaVM tests compile against it.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "BoxedProbe")
public class BoxedProbe {
  public native void take(Double value);

  public native void takeNumber(Number value);

  public native void takeBoolean(Boolean value);

  public static native String typeOf(Double value);

  public native Double ratio();

  public native Number count();

  public native Boolean visible();

  public native Double missing();

  @JsProperty
  public native void setSize(Double size);

  @JsProperty
  public native Double getSize();
}
