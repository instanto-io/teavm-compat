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
 * The document head. Present so {@link Document#getHead()} keeps GWT's return type, which is what
 * the theme switcher binds against when it swaps the stylesheet link.
 */
public class HeadElement extends Element {

  public static final String TAG = "head";

  public HeadElement(final HTMLElement element) {
    super(element);
  }

  /** Narrows a DOM element to the head type. */
  public static HeadElement as(final Element element) {
    return element == null ? null : new HeadElement(element.unwrap());
  }
}
