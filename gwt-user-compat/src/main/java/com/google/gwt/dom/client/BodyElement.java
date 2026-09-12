/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
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

/**
 * The document's body element.
 *
 * <p>GWT's {@code Document.getBody()} returns this rather than a plain element, and shared code
 * compiled against gwt-user carries that signature into the bytecode this layer is linked against.
 * Returning an {@code Element} instead therefore fails at link time rather than at compile time,
 * which is a slower way to find out.
 */
public class BodyElement extends Element {

  public static final String TAG = "body";

  public BodyElement(final HTMLElement element) {
    super(element);
  }

  /** Narrows an element to a body element, or null. */
  public static BodyElement as(final Element element) {
    return element == null ? null : new BodyElement(element.unwrap());
  }

  /** Whether this element is a body element. */
  public static boolean is(final Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }
}
