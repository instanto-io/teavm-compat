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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

/**
 * Element-backed base class, mirroring the GWT {@code UIObject} surface used by the Bootstrap
 * widget libraries.
 */
public abstract class UIObject {

  /** Style name applied to the primary element, kept first in the class list like GWT. */
  private String primaryStyleName = "";

  private Element element;

  public Element getElement() {
    return element;
  }

  @Deprecated
  public com.google.gwt.user.client.Element getStyleElement() {
    return com.google.gwt.user.client.Element.as(getElement());
  }

  protected void setElement(final Element element) {
    if (element == null) {
      throw new IllegalArgumentException("element must not be null");
    }
    this.element = element;
  }

  public void setStyleName(final String styleName) {
    getElement().setClassName(styleName);
  }

  public void setStyleName(final String styleName, final boolean add) {
    if (add) {
      addStyleName(styleName);
    } else {
      removeStyleName(styleName);
    }
  }

  public String getStyleName() {
    return getElement().getClassName();
  }

  /**
   * Adds a style name. Space-separated names are added individually, because {@code classList.add}
   * rejects values containing whitespace.
   */
  public void addStyleName(final String styleName) {
    if (styleName == null) {
      return;
    }
    for (final String part : styleName.trim().split("\\s+")) {
      if (!part.isEmpty()) {
        getElement().addClassName(part);
      }
    }
  }

  public void removeStyleName(final String styleName) {
    if (styleName == null) {
      return;
    }
    for (final String part : styleName.trim().split("\\s+")) {
      if (!part.isEmpty()) {
        getElement().removeClassName(part);
      }
    }
  }

  public void addStyleDependentName(final String styleSuffix) {
    setStyleDependentName(styleSuffix, true);
  }

  public void removeStyleDependentName(final String styleSuffix) {
    setStyleDependentName(styleSuffix, false);
  }

  public void setStyleDependentName(final String styleSuffix, final boolean add) {
    if (styleSuffix == null || styleSuffix.isEmpty()) {
      return;
    }
    final String primary = getStylePrimaryName();
    if (primary == null || primary.isEmpty()) {
      throw new IllegalStateException(
          "A primary style name is required before adding a dependent style");
    }
    setStyleName(primary + "-" + styleSuffix, add);
  }

  public void setStylePrimaryName(final String styleName) {
    if (primaryStyleName != null && !primaryStyleName.isEmpty()) {
      removeStyleName(primaryStyleName);
    }
    primaryStyleName = styleName == null ? "" : styleName;
    addStyleName(primaryStyleName);
  }

  public String getStylePrimaryName() {
    return primaryStyleName;
  }

  public void setVisible(final boolean visible) {
    setVisible(getElement(), visible);
  }

  public boolean isVisible() {
    return isVisible(getElement());
  }

  public static void setVisible(
      final com.google.gwt.dom.client.Element element, final boolean visible) {
    element.getStyle().setDisplay(visible ? null : Style.Display.NONE);
  }

  public static boolean isVisible(final com.google.gwt.dom.client.Element element) {
    return !"none".equals(element.getStyle().getProperty("display"));
  }

  public void setTitle(final String title) {
    if (title == null || title.isEmpty()) {
      getElement().removeAttribute("title");
    } else {
      getElement().setAttribute("title", title);
    }
  }

  public String getTitle() {
    return getElement().getAttribute("title");
  }

  public void setWidth(final String width) {
    getElement().getStyle().setProperty("width", width);
  }

  public void setHeight(final String height) {
    getElement().getStyle().setProperty("height", height);
  }

  public void setId(final String id) {
    getElement().setId(id);
  }

  public String getId() {
    return getElement().getId();
  }

  /**
   * Event sinking is a GWT bookkeeping step for its central event dispatcher. Handlers here attach
   * real DOM listeners on registration, so there is nothing to pre-declare; the mask is recorded so
   * {@code getEventsSunk} stays truthful.
   */
  public void sinkEvents(final int eventBitsToAdd) {
    eventsSunk |= eventBitsToAdd;
  }

  public void unsinkEvents(final int eventBitsToRemove) {
    eventsSunk &= ~eventBitsToRemove;
  }

  public int getEventsSunk() {
    return eventsSunk;
  }

  private int eventsSunk;

  public void setAccessKey(final String key) {
    if (key == null || key.isEmpty()) {
      getElement().removeAttribute("accesskey");
    } else {
      getElement().setAttribute("accesskey", key);
    }
  }

  public int getTabIndex() {
    return getElement().getPropertyInt("tabIndex");
  }

  public void setTabIndex(final int index) {
    getElement().setPropertyInt("tabIndex", index);
  }

  public void ensureDebugId(final String id) {
    onEnsureDebugId(id);
  }

  protected void onEnsureDebugId(final String baseId) {
    getElement().setId(DEBUG_ID_PREFIX + baseId);
  }

  /** Sets a debug id on a nested element, as GWT's static helper does. */
  public static void ensureDebugId(
      final com.google.gwt.dom.client.Element element, final String baseId) {
    if (element != null) {
      element.setId(DEBUG_ID_PREFIX + baseId);
    }
  }

  public static void ensureDebugId(
      final com.google.gwt.dom.client.Element element, final String baseId, final String id) {
    if (element != null) {
      element.setId(DEBUG_ID_PREFIX + baseId + "-" + id);
    }
  }

  public static final String DEBUG_ID_PREFIX = "debug-";

  /** The rendered width of this object's element, in pixels. */
  public int getOffsetWidth() {
    return getElement().getOffsetWidth();
  }

  /** The rendered height of this object's element, in pixels. */
  public int getOffsetHeight() {
    return getElement().getOffsetHeight();
  }
}
