package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.core.client.JavaScriptObject;
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
 * JsInterop code hands GWT DOM objects to libraries as they are, because on GWT an Element is the
 * DOM node. These check that jsinterop-base passes the node, not the TeaVM wrapper.
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

  @Test
  public void nativeNumbersKeepJavaScriptIntegerText() {
    Object slide = Js.cast(integralSlide());
    assertTrue(slide instanceof Number);
    assertEquals("1", slide.toString());
    assertEquals(1, ((Number) slide).intValue());

    Object fractional = Js.cast(fractionalSlide());
    assertTrue(fractional instanceof Number);
    assertEquals("1.5", fractional.toString());
    assertEquals("1", JavaScriptObject.of(integralSlide()).toString());
    assertEquals("1.5", JavaScriptObject.of(fractionalSlide()).toString());
  }

  @Test
  public void asAnyConvertsJavaObjectArraysToNativeArrays() {
    assertTrue(isNativeArguments(Js.asAny(new Object[] {1, false})));
  }

  @JSBody(
      params = "value",
      script =
          "return Array.isArray(value) && value.length === 2 && value[0] === 1 && value[1] === false;")
  private static native boolean isNativeArguments(JSObject value);

  @JSBody(script = "return 1;")
  private static native JSObject integralSlide();

  @JSBody(script = "return 1.5;")
  private static native JSObject fractionalSlide();

  @JSBody(
      params = {"value", "id"},
      script = "return value instanceof HTMLDivElement && value.id === id;")
  private static native boolean isDivWithId(JSObject value, String id);

  @JSBody(
      params = {"options", "key", "id"},
      script = "var v = options[key]; return v instanceof HTMLDivElement && v.id === id;")
  private static native boolean optionIsDivWithId(JSObject options, String key, String id);
}
