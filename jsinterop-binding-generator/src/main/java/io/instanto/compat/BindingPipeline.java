package io.instanto.compat;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import java.util.Collection;
import java.util.List;

/** The common source-adaptation passes used by the CLI and Maven plugin. */
public final class BindingPipeline {
  private final NativeCallbackBindings callbacks;
  private final JsInteropNames names;
  private final ErasedCallbackArguments erasedCallbacks;
  private final boolean legacyJsni;
  private final GwtOverlayTypes overlays;
  private final NativeInheritedResults inheritedResults;

  public BindingPipeline(Collection<String> sources, boolean legacyJsni) {
    this.legacyJsni = legacyJsni;
    // All five indexes inspect the same original declarations. Keep a single syntax tree
    // per source; only the later, per-file transformation needs the lexical token stream.
    JavaParser parser =
        new JavaParser(
            new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setStoreTokens(false));
    List<CompilationUnit> declarations =
        sources.stream()
            .map(
                source -> {
                  var parsed = parser.parse(source);
                  if (!parsed.isSuccessful()) throw new ParseProblemException(parsed.getProblems());
                  return parsed.getResult().orElseThrow();
                })
            .toList();
    inheritedResults = new NativeInheritedResults(declarations);
    overlays = new GwtOverlayTypes(declarations);
    names = new JsInteropNames(declarations);
    erasedCallbacks = new ErasedCallbackArguments(declarations);
    callbacks = new NativeCallbackBindings(declarations);
  }

  public String transform(String source) {
    return withJava17(
        () -> {
          String nativeSource = legacyJsni ? LegacyNativeBodies.transform(source) : source;
          return callbacks.transform(
              GenerateBindings.transform(
                  overlays.transform(
                      names.transform(
                          erasedCallbacks.transform(inheritedResults.transform(nativeSource)))),
                  overlays.types()));
        });
  }

  /** Modern GWT accepts Java 17 sources. Keep parser settings local to each adaptation call. */
  private static <T> T withJava17(java.util.function.Supplier<T> action) {
    var configuration = com.github.javaparser.StaticJavaParser.getParserConfiguration();
    var previous = configuration.getLanguageLevel();
    configuration.setLanguageLevel(com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_17);
    try {
      return action.get();
    } finally {
      configuration.setLanguageLevel(previous);
    }
  }
}
