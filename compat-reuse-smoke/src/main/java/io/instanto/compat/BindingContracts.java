package io.instanto.compat;

import elemental2.core.JsArray;
import elemental2.core.JsDate;
import elemental2.dom.*;
import jsinterop.base.*;

/** Native browser compatibility assertions. */
public final class BindingContracts {
  static void check(boolean condition, String name) {
    if (!condition) throw new IllegalStateException("Binding contract: " + name);
  }

  public static void run() {
    HTMLElement element = (HTMLElement) DomGlobal.document.createElement("div");
    element.id = "property-probe";
    check("property-probe".equals(element.id), "inherited field read/write");
    check(new JsDate(0.0).getTime() == 0, "native Date constructor");
    JsArray<String> array = new JsArray<>();
    array.push("first");
    array.push("second");
    check(array.length == 2 && "second".equals(array.getAt(1)), "generic array values");
    JsPropertyMap<Object> map = JsPropertyMap.of();
    Object javaValue = new java.util.ArrayList<String>();
    map.set("java", javaValue);
    check(map.get("java") == javaValue, "Java value identity through JS property map");
    map.set("text", "hello");
    check("hello".equals(map.getAsAny("text").asString()), "string conversion");
    check(map.has("text"), "map presence");
    map.delete("text");
    check(!map.has("text"), "map deletion");
    check("undefined".equals(Js.typeof(Js.undefined())), "undefined");
    check("object".equals(Js.typeof(null)), "null");
    int[] calls = {0};
    EventListener listener = e -> calls[0]++;
    element.addEventListener("probe", listener);
    element.dispatchEvent(new Event("probe"));
    element.removeEventListener("probe", listener);
    element.dispatchEvent(new Event("probe"));
    check(calls[0] == 1, "callback removal identity");
    element.addEventListener("probe", listener, true);
    element.dispatchEvent(new Event("probe"));
    element.removeEventListener("probe", listener, true);
    element.dispatchEvent(new Event("probe"));
    check(calls[0] == 2, "capture callback removal identity");
    CustomEventInit<Object> init = CustomEventInit.create();
    init.setDetail(javaValue);
    CustomEvent<Object> event = new CustomEvent<>("detail", init);
    check(event.detail == javaValue, "custom event Java detail identity");
  }
}
