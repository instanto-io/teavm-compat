package io.instanto.teavm.modules;

import io.instanto.compat.GenerateBindings;
import io.instanto.compat.LegacyNativeBodies;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Rewrites GWT client sources so TeaVM compiles them unchanged in meaning.
 *
 * <p>JsInterop declarations become TeaVM JSO, and GWT DOM wrappers are bridged where they
 * cross into JavaScript. With {@code legacyJsni}, JSNI bodies become {@code @JSBody} first;
 * a body that calls into Java through {@code @Class::member} cannot be translated and fails
 * the build rather than being dropped. Only {@code .java} files are written.
 */
final class SourceAdapter {
  private SourceAdapter() {}

  /** Adapts every Java source below {@code input} into {@code output}; returns how many. */
  static int adapt(Path input, Path output, boolean legacyJsni) throws IOException {
    Path in = input.toAbsolutePath().normalize();
    Path out = output.toAbsolutePath().normalize();
    if (in.startsWith(out) || out.startsWith(in))
      throw new IllegalArgumentException("Source and output roots must be separate");
    if (!Files.isDirectory(in)) throw new IllegalArgumentException("No source root at " + in);
    clear(out);
    List<Path> sources;
    try (Stream<Path> files = Files.walk(in)) {
      sources = files.filter(f -> f.toString().endsWith(".java")).sorted().toList();
    }
    for (Path source : sources) {
      Path relative = in.relativize(source);
      String text = Files.readString(source, StandardCharsets.UTF_8);
      String adapted;
      try {
        adapted = GenerateBindings.transform(legacyJsni ? LegacyNativeBodies.transform(text) : text);
      } catch (RuntimeException failure) {
        throw new IllegalArgumentException(relative + ": " + failure.getMessage(), failure);
      }
      Path target = out.resolve(relative);
      Files.createDirectories(target.getParent());
      Files.writeString(target, adapted, StandardCharsets.UTF_8);
    }
    return sources.size();
  }

  /** Removes earlier output so a source deleted upstream does not linger. */
  private static void clear(Path out) throws IOException {
    if (!Files.exists(out)) return;
    try (Stream<Path> files = Files.walk(out)) {
      for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
        try {
          Files.delete(file);
        } catch (IOException failure) {
          throw new UncheckedIOException(failure);
        }
      }
    }
  }
}
