/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * %%
 * Reimplements, over TeaVM's JSO libraries, part of the GWT client API. Class,
 * method and package names follow GWT (https://github.com/gwtproject/gwt),
 * Copyright (C) The GWT Project Authors, licensed under the Apache License,
 * Version 2.0. No GWT source is included.
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
package com.google.gwt.regexp.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Regular expression, backed by {@code java.util.regex} under TeaVM. */
public class RegExp {

  private final Pattern pattern;

  private RegExp(final Pattern pattern) {
    this.pattern = pattern;
  }

  public static RegExp compile(final String regex) {
    return new RegExp(Pattern.compile(regex));
  }

  public static RegExp compile(final String regex, final String flags) {
    int f = 0;
    if (flags != null) {
      if (flags.indexOf('i') >= 0) {
        f |= Pattern.CASE_INSENSITIVE;
      }
      if (flags.indexOf('m') >= 0) {
        f |= Pattern.MULTILINE;
      }
    }
    return new RegExp(Pattern.compile(regex, f));
  }

  public boolean test(final String input) {
    return input != null && pattern.matcher(input).find();
  }

  public MatchResult exec(final String input) {
    if (input == null) {
      return null;
    }
    final Matcher matcher = pattern.matcher(input);
    return matcher.find() ? new MatchResult(matcher) : null;
  }

  public String getSource() {
    return pattern.pattern();
  }

  public String replace(final String input, final String replacement) {
    return input == null ? null : pattern.matcher(input).replaceAll(replacement);
  }
}
