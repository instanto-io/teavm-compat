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

/** The outcome of a successful {@link RegExp} match. */
public class MatchResult {

  private final Matcher matcher;

  MatchResult(final Matcher matcher) {
    this.matcher = matcher;
  }

  public String getGroup(final int index) {
    return matcher.group(index);
  }

  public int getGroupCount() {
    return matcher.groupCount() + 1;
  }

  public int getIndex() {
    return matcher.start();
  }

  public String getInput() {
    return matcher.group();
  }
}
