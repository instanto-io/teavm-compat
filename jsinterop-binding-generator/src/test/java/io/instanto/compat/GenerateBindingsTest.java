package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.*;
import org.junit.Test;

public class GenerateBindingsTest {
  @org.junit.Test
  public void wrapsNativeDomPropertyAccessors() {
    String result =
        GenerateBindings.transform(
            "import com.google.gwt.dom.client.Element; import jsinterop.annotations.*; @JsType(isNative=true) public class Event { @JsProperty public Element currentTarget; @JsOverlay public final Element getCurrentTarget() { return currentTarget; } }");
    assertTrue(result.contains("@JSProperty(\"currentTarget\")"));
    assertTrue(result.contains("Element.as(com.google.gwt.core.client.JavaScriptObject.of("));
    assertTrue(
        StaticJavaParser.parse(result).findAll(MethodDeclaration.class).stream()
            .anyMatch(
                method ->
                    method.isNative()
                        && method.getTypeAsString().equals("org.teavm.jso.dom.html.HTMLElement")));
    assertFalse(result.contains("@JSMethod(\"getCurrentTarget\")"));
  }

  @Test
  public void bridgesNativeEventsAtNativeBoundaries() {
    String result =
        GenerateBindings.transform(
            "import com.google.gwt.dom.client.NativeEvent; import jsinterop.annotations.*;"
                + " @JsType(isNative=true,namespace=JsPackage.GLOBAL,name=\"Object\") class Events {"
                + " static native NativeEvent assign(Object value);"
                + " native void dispatch(NativeEvent event); }");
    assertTrue(result, result.contains("com.google.gwt.dom.client.NativeEvent.wrap("));
    assertTrue(result, result.contains("event == null ? null : event.unwrap()"));
    assertTrue(
        StaticJavaParser.parse(result).findAll(MethodDeclaration.class).stream()
            .anyMatch(
                method ->
                    method.isNative()
                        && method.getTypeAsString().equals("org.teavm.jso.dom.events.Event")));
  }

  @Test
  public void bridgesGwtArraysEventsAndUserElementsInJsniBodies() {
    String result =
        GenerateBindings.transform(
            LegacyNativeBodies.transform(
                "import com.google.gwt.core.client.JsArrayNumber;"
                    + " import com.google.gwt.user.client.Element;"
                    + " import com.google.gwt.user.client.Event;"
                    + " class Slider {"
                    + " static native JsArrayNumber values(Element e) /*-{ return e.v; }-*/;"
                    + " static native Element handle(Event event) /*-{ return event.target; }-*/; }"));
    assertTrue(result, result.contains("JsArrayNumber.of("));
    assertTrue(result, result.contains("com.google.gwt.user.client.Element.as("));
    assertTrue(result, result.contains("event == null ? null : event.unwrap()"));
    assertTrue(result, result.contains("e == null ? null : e.unwrap()"));
  }

  @Test
  public void preservesJavaWidgetLogic() {
    String source =
        "package demo; public class Counter { private int value; public int next(){return ++value;}"
            + " }";
    assertEquals(source, GenerateBindings.transform(source));
  }

  @Test
  public void mapsGlobalFieldsAndCallbacks() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*;"
                + " @JsType(isNative=true,name=\"goog.global\",namespace=JsPackage.GLOBAL) public"
                + " class Globals {public static Object document; @JsFunction public interface"
                + " Listener {void call();}} ");
    var cu = StaticJavaParser.parse(result);
    var field = cu.findFirst(FieldDeclaration.class).orElseThrow();
    assertFalse(field.isAnnotationPresent("JSTopLevel"));
    assertTrue(field.isAnnotationPresent("JSProperty"));
    assertTrue(result.contains("@JSFunctor"));
    assertFalse(result.contains("@JsType"));
  }

  @Test
  public void mapsNamespacedConstructorsAndStaticProperties() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*;"
                + " @JsType(isNative=true,name=\"NumberFormat\",namespace=\"Intl\") public class"
                + " Format {public static String supported; public Format(String locale){} public"
                + " native String format(double v);}");
    var cu = StaticJavaParser.parse(result);
    assertEquals(1, cu.getClassByName("Format").orElseThrow().getAnnotations().size());
    assertTrue(result.contains("globalThis.Intl.NumberFormat"));
    assertTrue(
        cu.findFirst(FieldDeclaration.class).orElseThrow().isAnnotationPresent("JSProperty"));
  }

  @Test
  public void addsZeroArgumentArrayConstructor() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*; @JsType(isNative=true,name=\"Array\") public class"
                + " JsArray<T> {public JsArray(T... args){}}");
    assertTrue(
        StaticJavaParser.parse(result).findAll(ConstructorDeclaration.class).stream()
            .anyMatch(c -> c.getParameters().isEmpty()));
  }

  @Test
  public void doesNotWriteNativeGlobalForJavaConstantAlias() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*; @JsType(isNative=true,name=\"goog.global\") class"
                + " Globals {@JsOverlay public static final Object document=Other.document;}");
    var f = StaticJavaParser.parse(result).findFirst(FieldDeclaration.class).orElseThrow();
    assertFalse(f.isAnnotationPresent("JSProperty"));
    assertTrue(f.getVariable(0).getInitializer().isPresent());
  }

  @Test
  public void normalizesObjectVarargsAtNativeBoundary() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*; @JsType(isNative=true,name=\"Console\") class Console"
                + " {public native void info(Object... args);}");
    assertTrue(result.contains("Js.asAny"));
    assertTrue(result.contains("@JSMethod(\"info\")"));
    assertTrue(result.contains("JSObject... args"));
  }

  @Test
  public void usesSafeJavascriptParametersAndArrayApplicationForNamespacedVarargs() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*; @JsType(isNative=true,name=\"Object\") class Query {"
                + " @JsMethod(namespace=\"$\") public static native Object proxy(Object function, Object... arguments); }");
    var owner = StaticJavaParser.parse(result).getClassByName("Query").orElseThrow();
    var wrapper = owner.getMethodsByName("proxy").get(0);
    assertFalse(wrapper.isAnnotationPresent("JSBody"));
    var bridge = owner.getMethodsByName("$nativeVarargs0").get(0);
    assertFalse(bridge.isAnnotationPresent("JSMethod"));
    var body = bridge.getAnnotationByName("JSBody").orElseThrow().asNormalAnnotationExpr();
    String script =
        body.getPairs().stream()
            .filter(p -> p.getNameAsString().equals("script"))
            .findFirst()
            .orElseThrow()
            .getValue()
            .asStringLiteralExpr()
            .asString();
    assertEquals("return globalThis.$[\"proxy\"].apply(globalThis.$,[arg0].concat(arg1));", script);
    assertEquals(
        "{ \"arg0\", \"arg1\" }",
        body.getPairs().stream()
            .filter(p -> p.getNameAsString().equals("params"))
            .findFirst()
            .orElseThrow()
            .getValue()
            .toString());
  }

  @Test
  public void preservesFunctorTypeBeforeUnionErasure() {
    String result =
        GenerateBindings.transform(
            "import jsinterop.annotations.*; import jsinterop.base.Js;"
                + " @JsType(isNative=true,name=\"Object\") class A { @JsFunction interface"
                + " CallbackFn {void call();} @JsOverlay static Object wrap(CallbackFn fn){return"
                + " Js.<CallbackUnionType>uncheckedCast(fn);} }");
    assertTrue(result.contains("$wrapCallbackFn(fn)"));
    assertTrue(result.contains("@JSBody"));
  }
}
