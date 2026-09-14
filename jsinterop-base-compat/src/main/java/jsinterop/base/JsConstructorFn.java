package jsinterop.base;

import org.teavm.jso.*;

public interface JsConstructorFn<T> extends JSObject {
  @JSBody(params = "args", script = "return Reflect.construct(this,args);")
  T construct(Object... args);
}
