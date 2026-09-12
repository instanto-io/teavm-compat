package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.*;
import org.junit.Test;

public class GenerateBindingsTest {
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
