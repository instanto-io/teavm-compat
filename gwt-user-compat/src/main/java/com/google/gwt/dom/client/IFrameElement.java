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

/** Typed browser view of a iframe element. */
public class IFrameElement extends Element {
  public static final String TAG = "iframe";

  public IFrameElement(HTMLElement element) {
    super(element);
  }

  public static IFrameElement as(Element element) {
    if (!is(element)) throw new IllegalArgumentException("Expected a iframe element");
    return new IFrameElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }

  public int getFrameBorder() {
    return getPropertyInt("frameBorder");
  }

  public int getMarginHeight() {
    return getPropertyInt("marginHeight");
  }

  public int getMarginWidth() {
    return getPropertyInt("marginWidth");
  }

  public String getName() {
    return getPropertyString("name");
  }

  public String getScrolling() {
    return getPropertyString("scrolling");
  }

  public String getSrc() {
    return getPropertyString("src");
  }

  public boolean isNoResize() {
    return getPropertyBoolean("noResize");
  }

  public void setFrameBorder(int frameBorder) {
    setPropertyInt("frameBorder", frameBorder);
  }

  public void setMarginHeight(int marginHeight) {
    setPropertyInt("marginHeight", marginHeight);
  }

  public void setMarginWidth(int marginWidth) {
    setPropertyInt("marginWidth", marginWidth);
  }

  public void setName(String name) {
    setPropertyString("name", name);
  }

  public void setNoResize(boolean noResize) {
    setPropertyBoolean("noResize", noResize);
  }

  public void setScrolling(String scrolling) {
    setPropertyString("scrolling", scrolling);
  }

  public void setSrc(String src) {
    setPropertyString("src", src);
  }
}
