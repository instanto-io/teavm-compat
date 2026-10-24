package io.instanto.teavm.modules;

import io.instanto.compat.BindingPipeline;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Rewrites GWT client sources so TeaVM compiles them unchanged in meaning.
 *
 * <p>JsInterop declarations become TeaVM JSO, and GWT DOM wrappers are bridged where they cross
 * into JavaScript. With {@code legacyJsni}, JSNI bodies become {@code @JSBody} first; a body that
 * calls into Java through {@code @Class::member} cannot be translated and fails the build rather
 * than being dropped. Only {@code .java} files are written.
 */
final class SourceAdapter {
  private SourceAdapter() {}

  /** Adapts every Java source below {@code input} into {@code output}; returns how many. */
  static int adapt(Path input, Path output, boolean legacyJsni) throws IOException {
    Path in = input.toRealPath();
    Path out = output.toFile().getCanonicalFile().toPath();
    if (in.startsWith(out) || out.startsWith(in))
      throw new IllegalArgumentException("Source and output roots must be separate");
    if (!Files.isDirectory(in)) throw new IllegalArgumentException("No source root at " + in);
    clear(out);
    List<Path> sources;
    try (Stream<Path> files = Files.walk(in)) {
      sources = files.filter(f -> f.toString().endsWith(".java")).sorted().toList();
    }
    java.util.Map<Path, String> inputs = new java.util.LinkedHashMap<>();
    for (Path source : sources)
      inputs.put(source, Files.readString(source, StandardCharsets.UTF_8));
    BindingPipeline pipeline = new BindingPipeline(inputs.values(), legacyJsni);
    for (Path source : sources) {
      Path relative = in.relativize(source);
      String text = inputs.get(source);
      String adapted;
      try {
        adapted = pipeline.transform(text);
      } catch (RuntimeException failure) {
        throw new IllegalArgumentException(relative + ": " + failure.getMessage(), failure);
      }
      Path target = out.resolve(relative);
      Files.createDirectories(java.util.Objects.requireNonNull(target.getParent()));
      Files.writeString(target, adapted, StandardCharsets.UTF_8);
    }
    return sources.size();
  }

  /** Removes earlier output so a source deleted upstream does not linger. */
  private static void clear(Path out) throws IOException {
    if (!Files.exists(out)) return;
    // Validate before deleting anything: a directory symlink could redirect later writes
    // outside the generated-source tree, even though the two roots are disjoint.
    try (Stream<Path> files = Files.walk(out)) {
      for (Path file : (Iterable<Path>) files::iterator) {
        if (Files.isSymbolicLink(file)) {
          throw new IOException("Generated-source output contains a symbolic link: " + file);
        }
      }
    }
    try (Stream<Path> files = Files.walk(out)) {
      for (Path file :
          (Iterable<Path>) files.filter(f -> f.toString().endsWith(".java"))::iterator) {
        Files.delete(file);
      }
    }
  }
}
