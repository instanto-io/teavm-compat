/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * Copyright 2010 Google Inc.
 * %%
 * Reimplements, over TeaVM's JSO libraries, part of the GWT client API. Class,
 * method and package names follow GWT (https://github.com/gwtproject/gwt),
 * Copyright (C) The GWT Project Authors, licensed under the Apache License,
 * Version 2.0. Entity escaping below retains the upstream implementation.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.google.gwt.safehtml.shared;

public final class SafeHtmlUtils {

  private SafeHtmlUtils() {}

  public static String htmlEscape(final String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  public static SafeHtml fromString(final String text) {
    final String escaped = htmlEscape(text);
    return () -> escaped;
  }

  public static SafeHtml fromTrustedString(final String html) {
    final String value = html == null ? "" : html;
    return () -> value;
  }

  public static SafeHtml fromSafeConstant(final String html) {
    return fromTrustedString(html);
  }

  /** Escapes markup but leaves existing character entities such as {@code &amp;} intact. */
  public static String htmlEscapeAllowEntities(String text) {
    StringBuilder escaped = new StringBuilder();

    boolean firstSegment = true;
    for (String segment : text.split("&", -1)) {
      if (firstSegment) {
        /*
         * The first segment is never part of an entity reference, so we always
         * escape it.
         * Note that if the input starts with an ampersand, we will get an empty
         * segment before that.
         */
        firstSegment = false;
        escaped.append(htmlEscape(segment));
        continue;
      }

      int entityEnd = segment.indexOf(';');
      if (entityEnd > 0
          && segment.substring(0, entityEnd).matches("[a-z]+|#[0-9]+|#x[0-9a-fA-F]+")) {
        // Append the entity without escaping.
        escaped.append("&").append(segment.substring(0, entityEnd + 1));

        // Append the rest of the segment, escaped.
        escaped.append(htmlEscape(segment.substring(entityEnd + 1)));
      } else {
        // The segment did not start with an entity reference, so escape the
        // whole segment.
        escaped.append("&amp;").append(htmlEscape(segment));
      }
    }

    return escaped.toString();
  }
}
