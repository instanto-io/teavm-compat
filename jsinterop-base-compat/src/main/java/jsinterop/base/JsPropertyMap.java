package jsinterop.base;

import org.teavm.jso.*;

public interface JsPropertyMap<T> extends JSObject {
  static <T> JsPropertyMap<T> of() {
    return Js.uncheckedCast(Js.emptyMap());
  }

  static <T> JsPropertyMap<T> of(String k, T v) {
    JsPropertyMap<T> m = of();
    m.set(k, v);
    return m;
  }

  static <T> JsPropertyMap<T> of(String k, T v, String k2, T v2) {
    JsPropertyMap<T> m = of(k, v);
    m.set(k2, v2);
    return m;
  }

  static <T> JsPropertyMap<T> of(String k, T v, String k2, T v2, String k3, T v3) {
    JsPropertyMap<T> m = of(k, v, k2, v2);
    m.set(k3, v3);
    return m;
  }

  default T get(String key) {
    return Js.cast(Js.fromNative(Js.mapGet(this, key)));
  }

  default void set(String key, T value) {
    Js.mapSet(this, key, Js.asAny(value));
  }

  default Any getAsAny(String key) {
    return Js.asAny(get(key));
  }

  default boolean has(String key) {
    return Js.mapHas(this, key);
  }

  default void delete(String key) {
    Js.mapDelete(this, key);
  }

  default void forEach(JsForEachCallbackFn cb) {
    Js.mapForEach(this, cb);
  }

  default Object nestedGet(String path) {
    return Js.mapNested(this, path);
  }

  default Any nestedGetAsAny(String path) {
    return Js.asAny(nestedGet(path));
  }
}
