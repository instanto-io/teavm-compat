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

/** Typed view of browser metadata. */
public class MetaElement extends Element {
  public static final String TAG = "meta";

  public MetaElement(HTMLElement element) {
    super(element);
  }

  public static MetaElement as(Element element) {
    if (!is(element)) throw new IllegalArgumentException("Expected a meta element");
    return new MetaElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }

  public String getContent() {
    return getPropertyString("content");
  }

  public void setContent(String value) {
    setPropertyString("content", value);
  }

  public String getHttpEquiv() {
    return getPropertyString("httpEquiv");
  }

  public void setHttpEquiv(String value) {
    setPropertyString("httpEquiv", value);
  }

  public String getName() {
    return getPropertyString("name");
  }

  public void setName(String value) {
    setPropertyString("name", value);
  }
}
