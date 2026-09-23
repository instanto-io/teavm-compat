package io.instanto.compat;

import org.teavm.jso.JSObject;

/**
 * A Java object that stands for a JavaScript value, such as gwt-user-compat's DOM and
 * JavaScriptObject wrappers.
 *
 * <p>On GWT those types are the JavaScript value, so JsInterop code hands one straight to a
 * library: {@code Js.asAny(element)}, or an element in an options map. {@link
 * jsinterop.base.Js#asAny} and {@link jsinterop.base.Js#asPropertyMap} pass the value this returns,
 * so the same code reaches the DOM node on TeaVM instead of its wrapper.
 */
public interface NativeHandle {
  JSObject unwrap();
}
