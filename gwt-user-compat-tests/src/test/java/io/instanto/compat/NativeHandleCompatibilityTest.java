package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

/**
 * JsInterop code hands GWT DOM objects to libraries as they are, because on GWT an Element is
 * the DOM node. These check that jsinterop-base passes the node, not the TeaVM wrapper.
 */
@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class NativeHandleCompatibilityTest {
  @Test
  public void asAnyPassesTheDomNode() {
    DivElement div = Document.get().createDivElement();
    div.setId("handle");
    assertTrue(isDivWithId(Js.asAny(div), "handle"));
  }

  @Test
  public void asPropertyMapReadsTheDomNode() {
    DivElement div = Document.get().createDivElement();
    div.setId("handle");
    assertEquals("DIV", Js.asPropertyMap(div).get("tagName"));
    assertEquals("handle", Js.asPropertyMap(div).get("id"));
  }

  @Test
  public void elementsInOptionMapsAreDomNodes() {
    DivElement div = Document.get().createDivElement();
    div.setId("gallery");
    JsPropertyMap<Object> options = JsPropertyMap.of("gallery", div);
    assertTrue(isDivWithId(Js.asAny(options.getAsAny("gallery")), "gallery"));
    assertTrue(optionIsDivWithId(Js.asAny(options), "gallery", "gallery"));
  }

  @JSBody(
      params = {"value", "id"},
      script = "return value instanceof HTMLDivElement && value.id === id;")
  private static native boolean isDivWithId(JSObject value, String id);

  @JSBody(
      params = {"options", "key", "id"},
      script = "var v = options[key]; return v instanceof HTMLDivElement && v.id === id;")
  private static native boolean optionIsDivWithId(JSObject options, String key, String id);
}
