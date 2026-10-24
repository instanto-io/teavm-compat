package io.instanto.widgets.processor;

import java.util.*;
import java.util.regex.*;

/** Rewrites class selectors, preserving strings, comments, URLs and declaration whitespace. */
final class CssSupport {
  record Styles(String css, Map<String, String> classes) {}

  private CssSupport() {}

  /** Positions outside CSS comments and quoted strings, for directive/URL recognition. */
  static boolean[] codePositions(String css) {
    boolean[] code = new boolean[css.length()];
    for (int i = 0; i < css.length(); ) {
      char c = css.charAt(i);
      if (c == '/' && i + 1 < css.length() && css.charAt(i + 1) == '*') {
        int end = css.indexOf("*/", i + 2);
        i = end < 0 ? css.length() : end + 2;
      } else if (c == '\'' || c == '"') {
        i++;
        while (i < css.length()) {
          char next = css.charAt(i++);
          if (next == '\\' && i < css.length()) i++;
          else if (next == c) break;
        }
      } else {
        code[i++] = true;
      }
    }
    return code;
  }

  static Styles scope(String css, String prefix) {
    Set<String> external = new HashSet<>();
    Matcher declarations = Pattern.compile("@external\\s+([^;]*);").matcher(css);
    boolean[] code = codePositions(css);
    StringBuffer withoutDirectives = new StringBuffer();
    while (declarations.find()) {
      if (!code[declarations.start()]) continue;
      external.addAll(Arrays.asList(declarations.group(1).trim().split("[\\s,]+")));
      declarations.appendReplacement(withoutDirectives, "");
    }
    css = declarations.appendTail(withoutDirectives).toString();
    Map<String, String> classes = new LinkedHashMap<>();
    StringBuilder result = new StringBuilder(), prelude = new StringBuilder();
    Deque<Boolean> blocks = new ArrayDeque<>();
    boolean values = false;
    for (int i = 0; i < css.length(); ) {
      char c = css.charAt(i);
      if (c == '\'' || c == '"' || (c == '/' && i + 1 < css.length() && css.charAt(i + 1) == '*')) {
        int end = i + 1;
        if (c == '/') {
          int stop = css.indexOf("*/", i + 2);
          end = stop < 0 ? css.length() : stop + 2;
        } else {
          while (end < css.length()) {
            char x = css.charAt(end++);
            if (x == '\\' && end < css.length()) end++;
            else if (x == c) break;
          }
        }
        result.append(css, i, end);
        if (!values && c != '/') prelude.append(css, i, end);
        i = end;
        continue;
      }
      if (c == '{') {
        String selector = prelude.toString().trim().toLowerCase(Locale.ROOT);
        blocks.push(values);
        values =
            !(selector.startsWith("@media")
                || selector.startsWith("@supports")
                || selector.startsWith("@layer")
                || selector.startsWith("@container")
                || selector.contains("keyframes"));
        prelude.setLength(0);
      } else if (c == '}') {
        values = blocks.isEmpty() ? false : blocks.pop();
        prelude.setLength(0);
      } else if (c == ';') prelude.setLength(0);
      if (!values
          && c == '.'
          && i + 1 < css.length()
          && (Character.isLetter(css.charAt(i + 1))
              || css.charAt(i + 1) == '_'
              || css.charAt(i + 1) == '-')) {
        int end = i + 2;
        while (end < css.length()
            && (Character.isLetterOrDigit(css.charAt(end))
                || css.charAt(end) == '_'
                || css.charAt(end) == '-')) end++;
        String name = css.substring(i + 1, end);
        boolean keep =
            external.contains(name)
                || external.contains("*")
                || external.stream()
                    .anyMatch(
                        x -> x.endsWith("*") && name.startsWith(x.substring(0, x.length() - 1)));
        String mapped = keep ? name : prefix + name;
        classes.put(name, mapped);
        result.append('.').append(mapped);
        prelude.append('.').append(name);
        i = end;
        continue;
      }
      result.append(c);
      if (!values && c != '{' && c != '}') prelude.append(c);
      i++;
    }
    return new Styles(result.toString(), classes);
  }
}
