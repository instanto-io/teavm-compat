package jsinterop.base;

import org.teavm.jso.*;

public interface Any extends JSObject {
  default String asString() {
    return Js.asString(this);
  }

  default boolean asBoolean() {
    return Js.asBoolean(this);
  }

  default double asDouble() {
    return Js.asDouble(this);
  }

  default int asInt() {
    return Js.asInt(this);
  }

  default float asFloat() {
    return Js.asFloat(this);
  }

  default long asLong() {
    return Js.asLong(this);
  }

  default short asShort() {
    return Js.asShort(this);
  }

  default byte asByte() {
    return Js.asByte(this);
  }

  default char asChar() {
    return Js.asChar(this);
  }

  default JsPropertyMap<Object> asPropertyMap() {
    return Js.asPropertyMap(this);
  }

  default JsArrayLike<Object> asArrayLike() {
    return Js.asArrayLike(this);
  }
}
