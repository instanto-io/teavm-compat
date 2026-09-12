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
package com.google.gwt.view.client;

/** A start index and length into a list of rows. */
public class Range {

  private final int start;
  private final int length;

  public Range(final int start, final int length) {
    this.start = start;
    this.length = length;
  }

  public int getStart() {
    return start;
  }

  public int getLength() {
    return length;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof Range)) {
      return false;
    }
    final Range that = (Range) other;
    return start == that.start && length == that.length;
  }

  @Override
  public int hashCode() {
    return 31 * start + length;
  }

  @Override
  public String toString() {
    return "Range(" + start + "," + length + ")";
  }
}
