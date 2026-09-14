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

/** Typed browser view of a img element. */
public class ImageElement extends Element {
  public static final String TAG = "img";

  public ImageElement(HTMLElement element) {
    super(element);
  }

  public static ImageElement as(Element element) {
    if (!is(element)) throw new IllegalArgumentException("Expected a img element");
    return new ImageElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }

  public String getAlt() {
    return getPropertyString("alt");
  }

  public int getHeight() {
    return getPropertyInt("height");
  }

  public String getSrc() {
    return getPropertyString("src");
  }

  public int getWidth() {
    return getPropertyInt("width");
  }

  public boolean isMap() {
    return getPropertyBoolean("isMap");
  }

  public void setAlt(String alt) {
    setPropertyString("alt", alt);
  }

  public void setHeight(int height) {
    setPropertyInt("height", height);
  }

  public void setIsMap(boolean isMap) {
    setPropertyBoolean("isMap", isMap);
  }

  public void setSrc(String src) {
    setPropertyString("src", src);
  }

  public void setUseMap(boolean useMap) {
    setPropertyBoolean("useMap", useMap);
  }

  public void setWidth(int width) {
    setPropertyInt("width", width);
  }

  public boolean useMap() {
    return getPropertyBoolean("useMap");
  }
}
