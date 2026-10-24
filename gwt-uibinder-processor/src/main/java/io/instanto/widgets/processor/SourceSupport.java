package io.instanto.widgets.processor;

import java.io.IOException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.tools.StandardLocation;

/** Common resource lookup and Java source literals for the javac generators. */
final class SourceSupport {
  private SourceSupport() {}

  static String annotation(Element element, String type, String member) {
    for (var annotation : element.getAnnotationMirrors()) {
      if (annotation.getAnnotationType().toString().equals(type)) {
        for (var value : annotation.getElementValues().entrySet())
          if (value.getKey().getSimpleName().contentEquals(member))
            return value.getValue().getValue().toString();
      }
    }
    return null;
  }

  static String quote(String value) {
    StringBuilder result = new StringBuilder("\"");
    for (char c : value.toCharArray()) {
      switch (c) {
        case '\\' -> result.append("\\\\");
        case '"' -> result.append("\\\"");
        case '\n' -> result.append("\\n");
        case '\r' -> result.append("\\r");
        case '\t' -> result.append("\\t");
        default -> {
          if (c < 32) result.append(String.format("\\%03o", (int) c));
          else result.append(c);
        }
      }
    }
    return result.append('"').toString();
  }

  static String stringExpression(String value) {
    StringBuilder result = new StringBuilder("new StringBuilder()");
    for (int i = 0; i < value.length(); ) {
      int end = Math.min(i + 4096, value.length());
      if (end < value.length()
          && Character.isHighSurrogate(value.charAt(end - 1))
          && Character.isLowSurrogate(value.charAt(end))) end--;
      result.append(".append(").append(quote(value.substring(i, end))).append(')');
      i = end;
    }
    return result.append(".toString()").toString();
  }

  static byte[] read(ProcessingEnvironment env, String pkg, String path) throws IOException {
    if (path.startsWith("/")) {
      pkg = "";
      path = path.substring(1);
    }
    for (var location :
        new StandardLocation[] {
          StandardLocation.SOURCE_PATH, StandardLocation.CLASS_PATH, StandardLocation.CLASS_OUTPUT
        }) {
      try (var input = env.getFiler().getResource(location, pkg, path).openInputStream()) {
        return input.readAllBytes();
      } catch (IOException missing) {
        /* Try the next compiler location. */
      }
    }
    throw new IOException("Missing resource " + pkg + "/" + path);
  }

  static String prefix(String qualifiedName) {
    // Encoding every UTF-16 code unit avoids collisions across packages and punctuation.
    StringBuilder prefix = new StringBuilder("ui");
    for (char c : qualifiedName.toCharArray()) prefix.append('_').append(Integer.toHexString(c));
    return prefix.append('-').toString();
  }
}
