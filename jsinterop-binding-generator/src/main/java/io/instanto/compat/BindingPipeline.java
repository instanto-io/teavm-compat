package io.instanto.compat;

import java.util.Collection;

/** The common source-adaptation passes used by the CLI and Maven plugin. */
public final class BindingPipeline {
  private final NativeCallbackBindings callbacks;
  private final JsInteropNames names;
  private final ErasedCallbackArguments erasedCallbacks;
  private final boolean legacyJsni;

  public BindingPipeline(Collection<String> sources, boolean legacyJsni) {
    this.legacyJsni = legacyJsni;
    names = new JsInteropNames(sources);
    erasedCallbacks = new ErasedCallbackArguments(sources);
    callbacks = new NativeCallbackBindings(sources);
  }

  public String transform(String source) {
    String nativeSource = legacyJsni ? LegacyNativeBodies.transform(source) : source;
    return callbacks.transform(
        GenerateBindings.transform(names.transform(erasedCallbacks.transform(nativeSource))));
  }
}
