package io.instanto.widgets.processor;

import java.util.*;

/** Rewrites class selectors, preserving strings, comments, URLs and declaration whitespace. */
final class CssSupport {
  record Styles(String css, Map<String, String> classes) {}

  private CssSupport() {}

  /** Positions outside CSS comments and quoted strings, for directive/URL recognition. */
  static boolean[] codePositions(String css) {
    boolean[] code = new boolean[css.length()];
    int offset = 0;
    while (offset < css.length()) {
      int end = literalEnd(css, offset);
      if (end == offset) code[offset++] = true;
      else offset = end;
    }
    return code;
  }

  private static int literalEnd(String css, int offset) {
    char quote = css.charAt(offset);
    if (css.startsWith("/*", offset)) {
      int end = css.indexOf("*/", offset + 2);
      return end < 0 ? css.length() : end + 2;
    }
    if (quote != '\'' && quote != '"') return offset;
    int end = offset + 1;
    while (end < css.length()) {
      char current = css.charAt(end++);
      if (current == quote) return end;
      if (current == '\\' && end < css.length()) end++;
    }
    return end;
  }

  static Styles scope(String css, String prefix) {
    Set<String> external = new HashSet<>();
    String stripped = removeExternalDirectives(css, external);
    return new Scope(stripped, prefix, external).rewrite();
  }

  private static String removeExternalDirectives(String css, Set<String> external) {
    boolean[] code = codePositions(css);
    StringBuilder result = new StringBuilder();
    int copied = 0;
    int start = css.indexOf("@external");
    while (start >= 0) {
      int content = start + "@external".length();
      if (code[start] && content < css.length() && Character.isWhitespace(css.charAt(content))) {
        int end = css.indexOf(';', content);
        if (end < 0) break;
        external.addAll(Arrays.asList(css.substring(content, end).trim().split("[\\s,]+")));
        result.append(css, copied, start);
        copied = end + 1;
        start = css.indexOf("@external", copied);
      } else {
        start = css.indexOf("@external", content);
      }
    }
    return result.append(css, copied, css.length()).toString();
  }

  record Url(int start, int end, String value) {}

  static List<Url> urls(String css) {
    List<Url> urls = new ArrayList<>();
    boolean[] code = codePositions(css);
    int start = css.indexOf("url(");
    while (start >= 0) {
      Url url = code[start] ? urlAt(css, start) : null;
      if (url != null) urls.add(url);
      // An unterminated token cannot contain another valid unquoted URL.
      int next = start + 4;
      if (code[start]) next = url == null ? css.indexOf(')', start + 4) + 1 : url.end();
      if (next == 0) break;
      start = css.indexOf("url(", next);
    }
    return urls;
  }

  private static Url urlAt(String css, int start) {
    int content = start + 4;
    while (content < css.length() && Character.isWhitespace(css.charAt(content))) content++;
    if (content == css.length()) return null;
    char first = css.charAt(content);
    if (first == '\'' || first == '"') {
      int end = literalEnd(css, content);
      if (end <= content + 1 || css.charAt(end - 1) != first) return null;
      String value = css.substring(content + 1, end - 1);
      while (end < css.length() && Character.isWhitespace(css.charAt(end))) end++;
      return end < css.length() && css.charAt(end) == ')'
          ? new Url(start, end + 1, value.trim())
          : null;
    }
    int end = css.indexOf(')', content);
    if (end < 0) return null;
    String value = css.substring(content, end).trim();
    return value.isEmpty() ? null : new Url(start, end + 1, value);
  }

  private static boolean identifierStart(char c) {
    return Character.isLetter(c) || c == '_' || c == '-';
  }

  private static boolean identifierPart(char c) {
    return Character.isLetterOrDigit(c) || c == '_' || c == '-';
  }

  private static final class Scope {
    private final String css;
    private final String prefix;
    private final Set<String> external;
    private final Map<String, String> classes = new LinkedHashMap<>();
    private final StringBuilder result = new StringBuilder();
    private final StringBuilder prelude = new StringBuilder();
    private final Deque<Boolean> blocks = new ArrayDeque<>();
    private boolean values;

    Scope(String css, String prefix, Set<String> external) {
      this.css = css;
      this.prefix = prefix;
      this.external = external;
    }

    Styles rewrite() {
      int offset = 0;
      while (offset < css.length()) offset = appendToken(offset);
      return new Styles(result.toString(), classes);
    }

    private int appendToken(int offset) {
      char c = css.charAt(offset);
      int literal = literalEnd(css, offset);
      if (literal != offset) {
        result.append(css, offset, literal);
        if (!values && c != '/') prelude.append(css, offset, literal);
        return literal;
      }
      updateBlock(c);
      if (!values
          && c == '.'
          && offset + 1 < css.length()
          && identifierStart(css.charAt(offset + 1))) return appendClass(offset);
      result.append(c);
      if (!values && c != '{' && c != '}') prelude.append(c);
      return offset + 1;
    }

    private void updateBlock(char c) {
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
        values = !blocks.isEmpty() && blocks.pop();
        prelude.setLength(0);
      } else if (c == ';') {
        prelude.setLength(0);
      }
    }

    private int appendClass(int offset) {
      int end = offset + 2;
      while (end < css.length() && identifierPart(css.charAt(end))) end++;
      String name = css.substring(offset + 1, end);
      String mapped = keep(name) ? name : prefix + name;
      classes.put(name, mapped);
      result.append('.').append(mapped);
      prelude.append('.').append(name);
      return end;
    }

    private boolean keep(String name) {
      return external.contains(name)
          || external.contains("*")
          || external.stream()
              .anyMatch(x -> x.endsWith("*") && name.startsWith(x.substring(0, x.length() - 1)));
    }
  }
}
