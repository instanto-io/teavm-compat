package com.google.gwt.typedarrays.shared;

import org.teavm.jso.JSBody;

/** Browser-native unsigned byte views; additional typed-array families are not yet supplied. */
public final class TypedArrays {
  private TypedArrays() {}

  @JSBody(params = "length", script = "return new ArrayBuffer(length);")
  public static native ArrayBuffer createArrayBuffer(int length);

  @JSBody(params = "length", script = "return new Uint8Array(length);")
  public static native Uint8Array createUint8Array(int length);

  @JSBody(params = "buffer", script = "return new Uint8Array(buffer);")
  public static native Uint8Array createUint8Array(ArrayBuffer buffer);

  @JSBody(
      params = {"buffer", "offset", "length"},
      script = "return new Uint8Array(buffer,offset,length);")
  public static native Uint8Array createUint8Array(ArrayBuffer buffer, int offset, int length);
}
