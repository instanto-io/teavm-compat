package io.instanto.compat;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

public class JsInteropNamesTest {
  @Test
  public void resolvesImportedAndConcatenatedNames() {
    String names =
        "package demo; interface Names { String ROOT = \"Charts\"; String TYPE = ROOT + \".Bar\"; }";
    String source =
        """
        package app;
        import demo.Names;
        import jsinterop.annotations.*;
        @JsType(isNative=true, name=Names.TYPE, namespace=JsPackage.GLOBAL)
        class Chart { @JsMethod(name=Names.ROOT) native void call(); }
        """;
    String result = new BindingPipeline(List.of(names, source), false).transform(source);
    assertTrue(result, result.contains("globalThis.Charts.Bar"));
    assertTrue(result, result.contains("@JSMethod(\"Charts\")"));
    assertFalse(result, result.contains("globalThis.Names"));
  }

  @Test
  public void resolvesStaticImportsAndLocalNames() {
    String names = "package demo; class Names { static final String VALUE = \"draw\"; }";
    String source =
        """
        package app;
        import static demo.Names.VALUE;
        import jsinterop.annotations.*;
        class Api {
          static final String NS = "Charts";
          @JsMethod(namespace=NS, name=VALUE) static native void render();
        }
        """;
    String result = new BindingPipeline(List.of(names, source), false).transform(source);
    assertTrue(result, result.contains("globalThis.Charts[\\\"draw\\\"]()"));
  }

  @Test
  public void unresolvedNamesFailInsteadOfGeneratingBrokenJavaScript() {
    String source =
        "import jsinterop.annotations.*; @JsType(isNative=true, name=Missing.NAME) class Api {}";
    assertThrows(
        IllegalArgumentException.class,
        () -> new BindingPipeline(List.of(source), false).transform(source));
  }
}
