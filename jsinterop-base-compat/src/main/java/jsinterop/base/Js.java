package jsinterop.base;

import org.teavm.jso.*;

public final class Js {
  private Js() {}

  @SuppressWarnings("unchecked")
  public static <T> T nativeCast(Object v) {
    return (T) (Object) asAny(v);
  }

  static Object fromNative(Object value) {
    if (value == null
        || value instanceof String
        || value instanceof Number
        || value instanceof Boolean) return value;
    String type = typeof(value);
    if ("undefined".equals(type)) return null;
    if ("string".equals(type)) return asString(value);
    if ("number".equals(type)) return asDouble(value);
    if ("boolean".equals(type)) return asBoolean(value);
    return value;
  }

  @JSBody(
      params = {"m", "k"},
      script = "return m[k];")
  static native Object mapGet(JSObject m, String k);

  @JSBody(
      params = {"m", "k", "v"},
      script = "m[k]=v;")
  static native void mapSet(JSObject m, String k, Object v);

  @JSBody(
      params = {"m", "k"},
      script = "return k in m;")
  static native boolean mapHas(JSObject m, String k);

  @JSBody(
      params = {"m", "k"},
      script = "delete m[k];")
  static native void mapDelete(JSObject m, String k);

  @JSBody(
      params = {"m", "cb"},
      script = "Object.keys(m).forEach(cb);")
  static native void mapForEach(JSObject m, JsForEachCallbackFn cb);

  @JSBody(
      params = {"m", "p"},
      script = "return p.split('.').reduce(function(v,k){return v == null ? null : v[k];},m);")
  static native Object mapNested(JSObject m, String p);

  @JSBody(params = "a", script = "return a.length;")
  static native int arrayLength(JSObject a);

  @JSBody(
      params = {"a", "n"},
      script = "a.length=n;")
  static native void arrayLength(JSObject a, int n);

  @JSBody(
      params = {"a", "i"},
      script = "return a[i];")
  static native Object arrayGet(JSObject a, int i);

  @JSBody(
      params = {"a", "i", "v"},
      script = "a[i]=v;")
  static native void arraySet(JSObject a, int i, Object v);

  @JSBody(
      params = {"a", "i"},
      script = "delete a[i];")
  static native void arrayDelete(JSObject a, int i);

  public static <T> JsConstructorFn<T> asConstructorFn(Class<T> type) {
    throw new UnsupportedOperationException(
        "Java Class to JS constructor mapping is not supported yet");
  }

  @JSBody(script = "return {};")
  static native JsPropertyMap<Object> emptyMap();

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object value) {
    return (T) fromNative(value);
  }

  public static <T> T uncheckedCast(Object value) {
    return cast(value);
  }

  @JSBody(
      params = {},
      script = "return undefined;")
  public static native Object undefined();

  @JSBody(
      params = {"value"},
      script = "return typeof value;")
  public static native String typeof(Object value);

  @JSBody(
      params = {"value"},
      script = "return !!value;")
  public static native boolean isTruthy(Object value);

  @JSBody(
      params = {"value"},
      script = "return !value;")
  public static native boolean isFalsy(Object value);

  @JSBody(
      params = {"a", "b"},
      script = "return a === b;")
  public static native boolean isTripleEqual(Object a, Object b);

  @JSBody(
      params = {"value"},
      script = "return Number(value);")
  public static native double coerceToDouble(Object value);

  public static String asString(Object value) {
    return value instanceof String ? (String) value : nativeString(value);
  }

  @JSBody(params = "value", script = "return value;")
  private static native String nativeString(Object value);

  @JSBody(
      params = {"value"},
      script = "return value;")
  public static native boolean asBoolean(Object value);

  @JSBody(
      params = {"value"},
      script = "return value;")
  public static native double asDouble(Object value);

  public static Any asAny(Object value) {
    if (value instanceof io.instanto.compat.NativeHandle)
      return objectValue(((io.instanto.compat.NativeHandle) value).unwrap());
    if (value instanceof String) return stringValue((String) value);
    if (value instanceof Number) return numberValue(((Number) value).doubleValue());
    if (value instanceof Boolean) return booleanValue((Boolean) value);
    return objectValue(value);
  }

  @JSBody(params = "v", script = "return v;")
  private static native Any stringValue(String v);

  @JSBody(params = "v", script = "return v;")
  private static native Any numberValue(double v);

  @JSBody(params = "v", script = "return v;")
  private static native Any booleanValue(boolean v);

  @JSBody(params = "v", script = "return v;")
  private static native Any objectValue(Object v);

  public static JsPropertyMap<Object> asPropertyMap(Object value) {
    return propertyMap(
        value instanceof io.instanto.compat.NativeHandle
            ? ((io.instanto.compat.NativeHandle) value).unwrap()
            : value);
  }

  @JSBody(
      params = {"value"},
      script = "return value;")
  private static native JsPropertyMap<Object> propertyMap(Object value);

  @JSBody(
      params = {"value"},
      script = "return value;")
  public static native JsArrayLike<Object> asArrayLike(Object value);

  @JSBody(
      params = {},
      script = "return globalThis;")
  public static native JsPropertyMap<Object> global();

  public static int asInt(Object v) {
    return (int) asDouble(v);
  }

  public static short asShort(Object v) {
    return (short) asDouble(v);
  }

  public static byte asByte(Object v) {
    return (byte) asDouble(v);
  }

  public static char asChar(Object v) {
    return (char) asDouble(v);
  }

  public static long asLong(Object v) {
    return (long) asDouble(v);
  }

  public static float asFloat(Object v) {
    return (float) asDouble(v);
  }

  public static int coerceToInt(Object v) {
    return (int) coerceToDouble(v);
  }
}
