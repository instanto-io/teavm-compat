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
package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;

/** Image widget rendered as an {@code img} element. */
public class Image extends Widget {

  public Image() {
    setElement(Document.get().createImageElement());
  }

  public Image(final String url) {
    this();
    setUrl(url);
  }

  public String getUrl() {
    return getElement().getAttribute("src");
  }

  public void setUrl(final String url) {
    getElement().setAttribute("src", url);
  }

  public String getAltText() {
    return getElement().getAttribute("alt");
  }

  public void setAltText(final String altText) {
    getElement().setAttribute("alt", altText == null ? "" : altText);
  }

  public Image(final com.google.gwt.safehtml.shared.SafeUri url) {
    this();
    setUrl(url);
  }

  public Image(final String url, final int left, final int top, final int width, final int height) {
    this();
    setUrlAndVisibleRect(url, left, top, width, height);
  }

  public Image(
      final com.google.gwt.safehtml.shared.SafeUri url,
      final int left,
      final int top,
      final int width,
      final int height) {
    this(url == null ? "" : url.asString(), left, top, width, height);
  }

  public Image(final com.google.gwt.resources.client.ImageResource resource) {
    this();
    setResource(resource);
  }

  public void setUrl(final com.google.gwt.safehtml.shared.SafeUri url) {
    setUrl(url == null ? "" : url.asString());
  }

  public void setResource(final com.google.gwt.resources.client.ImageResource resource) {
    if (resource == null) {
      return;
    }
    setUrlAndVisibleRect(
        resource.getURL(),
        resource.getLeft(),
        resource.getTop(),
        resource.getWidth(),
        resource.getHeight());
  }

  /**
   * Shows a rectangle of a larger image, as GWT's clipped mode does: the image becomes a sized
   * element with the source as a positioned background.
   */
  public void setUrlAndVisibleRect(
      final String url, final int left, final int top, final int width, final int height) {
    getElement().removeAttribute("src");
    getElement().getStyle().setProperty("background-image", "url(" + url + ")");
    getElement().getStyle().setProperty("background-position", -left + "px " + -top + "px");
    getElement().getStyle().setProperty("width", width + "px");
    getElement().getStyle().setProperty("height", height + "px");
  }

  public int getOriginLeft() {
    return 0;
  }

  public int getOriginTop() {
    return 0;
  }
}
