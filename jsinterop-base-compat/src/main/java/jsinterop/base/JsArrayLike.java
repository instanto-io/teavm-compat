package jsinterop.base;

import org.teavm.jso.*;

public interface JsArrayLike<T> extends JSObject {
  default int getLength() {
    return Js.arrayLength(this);
  }

  default void setLength(int length) {
    Js.arrayLength(this, length);
  }

  default T getAt(int index) {
    return Js.cast(Js.fromNative(Js.arrayGet(this, index)));
  }

  default void setAt(int index, T value) {
    Js.arraySet(this, index, Js.asAny(value));
  }

  default Any getAtAsAny(int index) {
    return Js.asAny(getAt(index));
  }

  default void delete(int index) {
    Js.arrayDelete(this, index);
  }

  default java.util.List<T> asList() {
    return new java.util.AbstractList<T>() {
      public T get(int i) {
        return getAt(i);
      }

      public int size() {
        return getLength();
      }

      public T set(int i, T value) {
        T old = getAt(i);
        setAt(i, value);
        return old;
      }
    };
  }
}
