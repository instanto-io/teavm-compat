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

/** Typed browser view of a style element. */
public class StyleElement extends Element {
  public static final String TAG = "style";

  public StyleElement(HTMLElement element) {
    super(element);
  }

  public static StyleElement as(Element element) {
    if (!is(element)) throw new IllegalArgumentException("Expected a style element");
    return new StyleElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }

  public String getCssText() {
    return getPropertyString("cssText");
  }

  public boolean getDisabled() {
    return getPropertyBoolean("disabled");
  }

  public String getMedia() {
    return getPropertyString("media");
  }

  public String getType() {
    return getPropertyString("type");
  }

  public boolean isDisabled() {
    return getPropertyBoolean("disabled");
  }

  public void setCssText(String cssText) {
    setPropertyString("cssText", cssText);
  }

  public void setDisabled(boolean disabled) {
    setPropertyBoolean("disabled", disabled);
  }

  public void setMedia(String media) {
    setPropertyString("media", media);
  }

  public void setType(String type) {
    setPropertyString("type", type);
  }
}
