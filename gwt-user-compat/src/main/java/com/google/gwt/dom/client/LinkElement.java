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

/** Typed browser view of a link element. */
public class LinkElement extends Element {
  public static final String TAG = "link";

  public LinkElement(HTMLElement element) {
    super(element);
  }

  public static LinkElement as(Element element) {
    if (!is(element)) throw new IllegalArgumentException("Expected a link element");
    return new LinkElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }

  public boolean getDisabled() {
    return getPropertyBoolean("disabled");
  }

  public String getHref() {
    return getPropertyString("href");
  }

  public String getHreflang() {
    return getPropertyString("hreflang");
  }

  public String getMedia() {
    return getPropertyString("media");
  }

  public String getRel() {
    return getPropertyString("rel");
  }

  public String getTarget() {
    return getPropertyString("target");
  }

  public String getType() {
    return getPropertyString("type");
  }

  public boolean isDisabled() {
    return getPropertyBoolean("disabled");
  }

  public void setDisabled(boolean disabled) {
    setPropertyBoolean("disabled", disabled);
  }

  public void setHref(String href) {
    setPropertyString("href", href);
  }

  public void setHreflang(String hreflang) {
    setPropertyString("hreflang", hreflang);
  }

  public void setMedia(String media) {
    setPropertyString("media", media);
  }

  public void setRel(String rel) {
    setPropertyString("rel", rel);
  }

  public void setTarget(String target) {
    setPropertyString("target", target);
  }

  public void setType(String type) {
    setPropertyString("type", type);
  }
}
