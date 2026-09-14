package io.instanto.compat;

import org.teavm.jso.*;

/** Weakly cached callback adapters preserve identity for native listener removal. */
public final class NativeCallbacks {
  private static final JSObject CACHE = create();

  private NativeCallbacks() {}

  @SuppressWarnings("unchecked")
  public static <T extends JSObject> T remember(Object callback, String shape, T adapter) {
    return (T) rememberNative(CACHE, callback, shape, adapter);
  }

  @JSBody(script = "return new WeakMap();")
  private static native JSObject create();

  @JSBody(
      params = {"cache", "key", "shape", "adapter"},
      script =
          "var entries=cache.get(key); if(!entries){entries=new Map();cache.set(key,entries);} if(!entries.has(shape)) entries.set(shape,adapter); return entries.get(shape);")
  private static native JSObject rememberNative(
      JSObject cache, Object key, String shape, JSObject adapter);
}
