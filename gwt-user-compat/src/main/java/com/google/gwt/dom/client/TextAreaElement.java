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
package com.google.gwt.dom.client;

import org.teavm.jso.dom.html.HTMLElement;

/** Typed view of a {@code <TextArea>}-family element. */
public class TextAreaElement extends Element {

  public TextAreaElement(final HTMLElement element) {
    super(element);
  }

  public static TextAreaElement as(final Element element) {
    return element == null ? null : new TextAreaElement(element.unwrap());
  }

  public String getValue() {
    return getPropertyString("value");
  }

  public void setValue(final String value) {
    setPropertyString("value", value);
  }

  public String getName() {
    return getPropertyString("name");
  }

  public void setName(final String name) {
    setPropertyString("name", name);
  }

  public int getRows() {
    return getPropertyInt("rows");
  }

  public void setRows(final int rows) {
    setPropertyInt("rows", rows);
  }

  public int getCols() {
    return getPropertyInt("cols");
  }

  public void setCols(final int cols) {
    setPropertyInt("cols", cols);
  }

  public static final String TAG = "textarea";

  /** True when the element is a {@code <textarea>}. */
  public static boolean is(final Element element) {
    if (element == null) {
      return false;
    }
    final String tag = element.getTagName();
    return "textarea".equalsIgnoreCase(tag);
  }
}
