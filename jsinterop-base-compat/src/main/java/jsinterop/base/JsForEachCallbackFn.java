package jsinterop.base;

import org.teavm.jso.*;

@JSFunctor
public interface JsForEachCallbackFn extends JSObject {
  void onKey(String key);
}
