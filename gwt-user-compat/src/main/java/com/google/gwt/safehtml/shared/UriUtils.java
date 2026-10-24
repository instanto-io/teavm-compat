package com.google.gwt.safehtml.shared;

/** URI checks used by generated SafeHtml templates. Explicit SafeUri values are trusted. */
public final class UriUtils {
  private UriUtils() {}

  public static boolean isSafeUri(String uri) {
    if (uri == null) return false;
    String normalized = uri.replaceAll("[\\x00-\\x20]", "");
    int colon = normalized.indexOf(':');
    int separator = normalized.length();
    for (char c : new char[] {'/', '?', '#'}) {
      int index = normalized.indexOf(c);
      if (index >= 0) separator = Math.min(separator, index);
    }
    if (colon < 0 || colon > separator) return true;
    String scheme = normalized.substring(0, colon).toLowerCase(java.util.Locale.ROOT);
    return java.util.Set.of("http", "https", "mailto", "ftp", "tel").contains(scheme);
  }

  public static String sanitizeUri(String uri) {
    return isSafeUri(uri) ? uri : "#";
  }

  public static SafeUri fromString(String uri) {
    String safe = sanitizeUri(uri);
    return () -> safe;
  }

  public static SafeUri fromSafeConstant(String uri) {
    return () -> uri;
  }

  public static SafeUri fromTrustedString(String uri) {
    return () -> uri;
  }
}
