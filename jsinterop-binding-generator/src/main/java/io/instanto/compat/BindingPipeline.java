package io.instanto.compat;

import java.util.Collection;

/** The common source-adaptation passes used by the CLI and Maven plugin. */
public final class BindingPipeline {
  private final NativeCallbackBindings callbacks;
  private final JsInteropNames names;
  private final ErasedCallbackArguments erasedCallbacks;
  private final boolean legacyJsni;
  private final GwtOverlayTypes overlays;

  public BindingPipeline(Collection<String> sources, boolean legacyJsni) {
    this.legacyJsni = legacyJsni;
    overlays = withJava17(() -> new GwtOverlayTypes(sources));
    names = withJava17(() -> new JsInteropNames(sources));
    erasedCallbacks = withJava17(() -> new ErasedCallbackArguments(sources));
    callbacks = withJava17(() -> new NativeCallbackBindings(sources));
  }

  public String transform(String source) {
    return withJava17(
        () -> {
          String nativeSource = legacyJsni ? LegacyNativeBodies.transform(source) : source;
          return callbacks.transform(
              GenerateBindings.transform(
                  overlays.transform(names.transform(erasedCallbacks.transform(nativeSource))),
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
