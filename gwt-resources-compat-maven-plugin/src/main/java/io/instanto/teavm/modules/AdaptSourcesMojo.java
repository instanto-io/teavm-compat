package io.instanto.teavm.modules;

import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Adapts unpacked GWT client sources for a TeaVM compile.
 *
 * <p>Libraries shared between GWT and TeaVM keep one source: JsInterop, and optionally JSNI without
 * Java callbacks, is rewritten for TeaVM here rather than maintained twice. Point the compiler at
 * {@code outputRoot} instead of {@code sourceRoot}.
 */
@Mojo(name = "adapt-sources", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public final class AdaptSourcesMojo extends AbstractMojo {
  /** GWT sources to adapt, typically unpacked from source JARs. */
  @Parameter(required = true)
  private File sourceRoot;

  /** Where the adapted Java sources are written. */
  @Parameter(defaultValue = "${project.build.directory}/teavm-sources", required = true)
  private File outputRoot;

  /** Also translate JSNI bodies to {@code @JSBody}. Bodies calling into Java fail the build. */
  @Parameter(defaultValue = "false")
  private boolean legacyJsni;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      int count = SourceAdapter.adapt(sourceRoot.toPath(), outputRoot.toPath(), legacyJsni);
      getLog().info("Adapted " + count + " sources for TeaVM into " + outputRoot);
    } catch (IOException | IllegalArgumentException | java.io.UncheckedIOException failure) {
      throw new MojoExecutionException(
          "Unable to adapt sources for TeaVM: " + failure.getMessage(), failure);
    }
  }
}
