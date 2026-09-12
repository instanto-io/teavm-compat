package javaemul.internal;

import jsinterop.base.JsArrayLike;

public final class ArrayStamper {
  private ArrayStamper() {}

  public static <T> T[] stampJavaTypeInfo(JsArrayLike<T> source, T[] reference) {
    T[] result = java.util.Arrays.copyOf(reference, source.getLength());
    for (int i = 0; i < result.length; i++) result[i] = source.getAt(i);
    return result;
  }
}
