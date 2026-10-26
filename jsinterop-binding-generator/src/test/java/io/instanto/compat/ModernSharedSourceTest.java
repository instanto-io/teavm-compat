package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import java.util.List;
import org.junit.Test;

public class ModernSharedSourceTest {
  @Test
  public void adaptsBindingsAlongsideRecordsWithoutChangingCallerParserSettings() {
    var configuration = StaticJavaParser.getParserConfiguration();
    var previous = configuration.getLanguageLevel();
    configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8);
    try {
      String recordSource =
          "package example; public record Selection(int anchor, int head) { public int length() { return Math.abs(head-anchor); } }";
      String binding =
          "package example; import jsinterop.annotations.*; @JsType(isNative=true, namespace=JsPackage.GLOBAL, name=\"Editor\") public class NativeEditor { public static native int length(); }";
      var pipeline = new BindingPipeline(List.of(recordSource, binding), false);
      assertEquals(ParserConfiguration.LanguageLevel.JAVA_8, configuration.getLanguageLevel());
      assertTrue(pipeline.transform(recordSource).contains("record Selection"));
      String adapted = pipeline.transform(binding);
      assertTrue(adapted, adapted.contains("JSClass"));
      assertTrue(adapted, adapted.contains("Editor"));
      assertEquals(ParserConfiguration.LanguageLevel.JAVA_8, configuration.getLanguageLevel());
      var malformedSources = List.of("record Broken(");
      assertThrows(RuntimeException.class, () -> new BindingPipeline(malformedSources, false));
      assertEquals(ParserConfiguration.LanguageLevel.JAVA_8, configuration.getLanguageLevel());
    } finally {
      configuration.setLanguageLevel(previous);
    }
  }
}
