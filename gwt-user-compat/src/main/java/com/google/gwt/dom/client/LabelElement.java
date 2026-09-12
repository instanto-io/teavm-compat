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

/** Typed view of a {@code <label>} element. */
public class LabelElement extends Element {

  public static final String TAG = "label";

  public LabelElement(final HTMLElement element) {
    super(element);
  }

  public static LabelElement as(final Element element) {
    return element == null ? null : new LabelElement(element.unwrap());
  }

  /** True when the element is a {@code <label>}. */
  public static boolean is(final Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }

  public String getHtmlFor() {
    return getAttribute("for");
  }

  public void setHtmlFor(final String htmlFor) {
    setAttribute("for", htmlFor);
  }
}
