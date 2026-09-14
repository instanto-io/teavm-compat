package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.StaticJavaParser;
import org.junit.Test;

public class LegacyNativeBindingsTest {
  @Test
  public void objectArgumentsAreUnboxedWithoutChangingTypedVarargs() {
    String source =
        "import jsinterop.annotations.*; @JsType(isNative=true,name=\"Object\") class Query { public native void prop(String name, Object value); public native void trigger(String type, Object... values); }";
    var owner =
        StaticJavaParser.parse(GenerateBindings.transform(source))
            .getClassByName("Query")
            .orElseThrow();
    var method = owner.getMethodsByName("prop").get(0);
    assertFalse(method.isNative());
    assertTrue(
        method.getBody().orElseThrow().toString().contains("jsinterop.base.Js.asAny(value)"));
    var bridge = owner.getMethodsByName("$gwtBridge0").get(0);
    assertEquals("jsinterop.base.Any", bridge.getParameter(1).getTypeAsString());
    assertTrue(owner.getMethodsByName("trigger").get(0).getParameter(1).isVarArgs());
    assertTrue(
        owner
            .getMethodsByName("trigger")
            .get(0)
            .getBody()
            .orElseThrow()
            .toString()
            .contains("Js.asAny(values[i])"));
    assertTrue(owner.getMethodsByName("$nativeVarargs0").get(0).isNative());
  }

  @Test
  public void objectReturnsAreConvertedWithoutChangingTheNativeMethodName() {
    String source =
        "import jsinterop.annotations.*; @JsType(isNative=true,name=\"Object\") class Query { public native Object val(); }";
    var owner =
        StaticJavaParser.parse(GenerateBindings.transform(source))
            .getClassByName("Query")
            .orElseThrow();
    var method = owner.getMethodsByName("val").get(0);
    assertFalse(method.isNative());
    assertTrue(method.getBody().orElseThrow().toString().contains("jsinterop.base.Js.cast"));
    var bridge = owner.getMethodsByName("$gwtBridge0").get(0);
    assertTrue(bridge.isNative());
    assertEquals(
        "@JSMethod(\"val\")", bridge.getAnnotationByName("JSMethod").orElseThrow().toString());
  }

  @Test
  public void staticMethodNamespacesReferToTheirJavascriptOwner() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*; @JsType(isNative=true) class Material { @JsMethod(namespace=\"Waves\") public static native void displayEffect(); }");
    assertTrue(result.contains("globalThis.Waves"));
    assertTrue(result.contains("@JSBody"));
  }

  @Test
  public void makesFreshCustomDomEventsForEachDispatch() {
    String result =
        GenerateBindings.transform(
            "import com.google.gwt.event.dom.client.DomEvent; class Input extends DomEvent<Handler> { static final Type<Handler> TYPE = new Type<>(\"input\",new Input()); }");
    assertTrue(result.contains("() -> new Input()"));
  }

  @Test
  public void translatesJsniAndBridgesDomArguments() {
    String source =
        "import com.google.gwt.dom.client.Element; class Geometry { public native double top(Element element) /*-{ return element.getBoundingClientRect().top + $wnd.scrollY; }-*/; }";
    String result = GenerateBindings.transform(LegacyNativeBodies.transform(source));
    assertTrue(result.contains("window.scrollY"));
    assertTrue(result.contains("element.unwrap()"));
    var bridge =
        StaticJavaParser.parse(result)
            .getClassByName("Geometry")
            .orElseThrow()
            .getMethodsByName("$gwtBridge0")
            .get(0);
    assertTrue(bridge.isPrivate() && bridge.isStatic() && bridge.isNative());
    assertTrue(result.contains("org.teavm.jso.dom.html.HTMLElement"));
  }

  @Test
  public void rejectsJsniJavaMemberReferences() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            LegacyNativeBodies.transform("class A { native void run() /*-{ @A::run()(); }-*/; }"));
  }

  @Test
  public void preservesTypedArraysAndStaticGlobalBindings() {
    String result =
        GenerateBindings.transform(
            "import com.google.gwt.dom.client.Element; import jsinterop.annotations.*; @JsType(isNative=true,name=\"Object\",namespace=GLOBAL) class Query { @JsProperty public static String version; public native Element[] get(); @JsMethod(namespace=GLOBAL,name=\"$\") public static native Query select(Element element); @JsOverlay public <T extends Query> T cast(){return (T)this;} }");
    assertTrue(result.contains("globalThis.Object"));
    assertFalse(result.contains("globalThis.GLOBAL"));
    assertTrue(result.contains("Element.as("));
    assertTrue(result.contains("element.unwrap()"));
    assertTrue(result.contains("@JSTopLevel"));
    var owner = StaticJavaParser.parse(result).getClassByName("Query").orElseThrow();
    assertTrue(owner.getMethodsByName("cast").isEmpty());
    assertEquals(1, owner.getFields().get(0).getAnnotations().size());
  }
}
