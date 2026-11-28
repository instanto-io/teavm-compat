package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.*;
import java.util.List;
import org.junit.Test;

/** Boxed numbers and booleans reach JavaScript as primitives, as they do under GWT. */
public class BoxedValuesTest {
  private static final String NATIVE =
      "package demo; import jsinterop.annotations.*;"
          + " @JsType(isNative=true,namespace=JsPackage.GLOBAL,name=\"Probe\") public class Probe {";

  private static String adapt(String source) {
    return new BindingPipeline(List.of(source), false).transform(source);
  }

  private static MethodDeclaration method(String result, String name) {
    return StaticJavaParser.parse(result).findAll(MethodDeclaration.class).stream()
        .filter(m -> m.getNameAsString().equals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError(name + " missing from\n" + result));
  }

  private static List<MethodDeclaration> bridges(String result) {
    return StaticJavaParser.parse(result).findAll(MethodDeclaration.class).stream()
        .filter(m -> m.getNameAsString().startsWith("$gwtBridge"))
        .toList();
  }

  @Test
  public void boxedParametersKeepTheirJavaTypeAndCrossAsPrimitives() {
    String result =
        adapt(
            NATIVE
                + " public native void scale(Double factor, Number size, Boolean visible,"
                + " int count); }");
    var scale = method(result, "scale");
    assertFalse(result, scale.isNative());
    assertEquals(
        List.of("Double", "Number", "Boolean", "int"),
        scale.getParameters().stream().map(p -> p.getTypeAsString()).toList());
    String body = scale.getBody().orElseThrow().toString();
    assertTrue(body, body.contains("jsinterop.base.Js.asAny(factor)"));
    assertTrue(body, body.contains("jsinterop.base.Js.asAny(size)"));
    assertTrue(body, body.contains("jsinterop.base.Js.asAny(visible)"));
    assertTrue(body, body.contains(", count)"));
    var bridge = bridges(result).get(0);
    assertTrue(bridge.isNative());
    assertTrue(result, bridge.isAnnotationPresent("JSMethod"));
    assertTrue(result, result.contains("@JSMethod(\"scale\")"));
    assertEquals(
        List.of("jsinterop.base.Any", "jsinterop.base.Any", "jsinterop.base.Any", "int"),
        bridge.getParameters().stream().map(p -> p.getTypeAsString()).toList());
  }

  @Test
  public void boxedResultsAreReadBackAsBoxesWithUndefinedAsNull() {
    String result =
        adapt(
            NATIVE
                + " public native Double ratio(); public native Number size();"
                + " public native Boolean visible(); public native Integer count(); }");
    for (var name : List.of("ratio", "size", "visible", "count")) {
      var method = method(result, name);
      assertFalse(name, method.isNative());
      String body = method.getBody().orElseThrow().toString();
      assertTrue(body, body.contains("$boxed == null || \"undefined\".equals("));
    }
    assertTrue(
        result,
        method(result, "ratio").getBody().orElseThrow().toString().contains("Double.valueOf("));
    assertTrue(
        result,
        method(result, "size").getBody().orElseThrow().toString().contains("Double.valueOf("));
    assertTrue(
        result,
        method(result, "visible").getBody().orElseThrow().toString().contains("Boolean.valueOf("));
    assertTrue(
        result,
        method(result, "count").getBody().orElseThrow().toString().contains("Integer.valueOf("));
    assertTrue(
        bridges(result).stream()
            .allMatch(b -> b.getTypeAsString().equals("jsinterop.base.Any") && b.isNative()));
  }

  @Test
  public void boxedPropertyAccessorsInNativeInterfacesAreConverted() {
    // The shape of three.js parameter objects: options read and written through accessors.
    String result =
        adapt(
            "package demo; import jsinterop.annotations.*;"
                + " @JsType(isNative=true,namespace=JsPackage.GLOBAL,name=\"Object\")"
                + " public interface Parameters {"
                + " @JsProperty void setSize(Number size); @JsProperty Number getSize();"
                + " @JsProperty Boolean isTransparent(); }");
    var setter = method(result, "setSize");
    assertTrue(result, setter.isDefault());
    assertEquals("Number", setter.getParameter(0).getTypeAsString());
    assertTrue(result, setter.getBody().orElseThrow().toString().contains("Js.asAny(size)"));
    assertEquals("Number", method(result, "getSize").getTypeAsString());
    assertTrue(result, result.contains("@JSProperty(\"size\")"));
    assertTrue(result, result.contains("@JSProperty(\"transparent\")"));
    assertTrue(
        result,
        method(result, "isTransparent")
            .getBody()
            .orElseThrow()
            .toString()
            .contains("Boolean.valueOf("));
  }

  @Test
  public void staticNativeMethodsAreConverted() {
    String result = adapt(NATIVE + " public static native Probe of(Double value); }");
    var of = method(result, "of");
    assertTrue(of.isStatic());
    assertFalse(of.isNative());
    assertTrue(result, of.getBody().orElseThrow().toString().contains("Js.asAny(value)"));
    assertTrue(bridges(result).get(0).isStatic());
  }

  @Test
  public void boxedNativeFieldsAreRejectedByName() {
    var failure =
        assertThrows(
            IllegalArgumentException.class,
            () -> adapt(NATIVE + " public Number size; public double opacity; }"));
    assertTrue(failure.getMessage(), failure.getMessage().contains("demo.Probe.size"));
    assertTrue(failure.getMessage(), failure.getMessage().contains("Declare it double"));
    var flag =
        assertThrows(IllegalArgumentException.class, () -> adapt(NATIVE + " public Boolean on; }"));
    assertTrue(flag.getMessage(), flag.getMessage().contains("Declare it boolean"));
  }

  @Test
  public void primitivesOverlaysAndLongsAreLeftAlone() {
    String source =
        NATIVE
            + " public double opacity; public boolean visible; public native double ratio(double r);"
            + " @JsOverlay public static final Double DEFAULT = 1.0;"
            + " @JsOverlay public final Double twice(Double v) { return v * 2; }"
            + " public native Long stamp(Long l); }";
    String result = adapt(source);
    assertTrue(result, bridges(result).isEmpty());
    assertTrue(result, method(result, "ratio").isNative());
    assertTrue(result, method(result, "stamp").isNative());
  }

  @Test
  public void aLibraryTypeNamedLikeABoxIsNotConverted() {
    String result =
        adapt(
            "package demo; import jsinterop.annotations.*; import demo.math.Number;"
                + " @JsType(isNative=true) public class Probe { public Number size;"
                + " public native void take(Number n); }");
    assertTrue(result, bridges(result).isEmpty());
    assertTrue(result, method(result, "take").isNative());
  }

  @Test
  public void plainJavaTypesAreUntouched() {
    String source =
        "package demo; public class Counter { public Double next(Double d) { return d; } }";
    assertEquals(source, adapt(source));
  }
}
