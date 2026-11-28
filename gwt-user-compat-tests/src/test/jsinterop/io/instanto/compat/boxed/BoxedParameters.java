package io.instanto.compat.boxed;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** A three.js-style parameter object, read and written through boxed accessors. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public interface BoxedParameters {
  @JsProperty
  void setSize(Number size);

  @JsProperty
  Number getSize();

  @JsProperty
  void setTransparent(Boolean transparent);

  @JsProperty
  Boolean getTransparent();
}
