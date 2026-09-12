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

public final class Style {
  public enum Unit {
    PX("px"),
    PCT("%"),
    EM("em"),
    REM("rem"),
    EX("ex"),
    PT("pt"),
    PC("pc"),
    IN("in"),
    CM("cm"),
    MM("mm");

    private final String suffix;

    Unit(final String suffix) {
      this.suffix = suffix;
    }

    /** The CSS unit suffix, e.g. {@code "px"}. */
    public String getType() {
      return suffix;
    }
  }

  public interface HasCssName {
    String getCssName();
  }

  public enum Display implements HasCssName {
    NONE("none"),
    BLOCK("block"),
    INLINE("inline"),
    INLINE_BLOCK("inline-block");

    private final String cssName;

    Display(final String cssName) {
      this.cssName = cssName;
    }

    @Override
    public String getCssName() {
      return cssName;
    }
  }

  private final HTMLElement element;

  Style(final HTMLElement element) {
    this.element = element;
  }

  public void setMarginTop(final double value, final Unit unit) {
    setProperty("margin-top", value, unit);
  }

  public void setMarginLeft(final double value, final Unit unit) {
    setProperty("margin-left", value, unit);
  }

  public void setMarginRight(final double value, final Unit unit) {
    setProperty("margin-right", value, unit);
  }

  public void setMarginBottom(final double value, final Unit unit) {
    setProperty("margin-bottom", value, unit);
  }

  public void setPaddingTop(final double value, final Unit unit) {
    setProperty("padding-top", value, unit);
  }

  public void setPaddingLeft(final double value, final Unit unit) {
    setProperty("padding-left", value, unit);
  }

  public void setPaddingRight(final double value, final Unit unit) {
    setProperty("padding-right", value, unit);
  }

  public void setPaddingBottom(final double value, final Unit unit) {
    setProperty("padding-bottom", value, unit);
  }

  public void setColor(final String color) {
    setProperty("color", color);
  }

  public void setFontSize(final double value, final Unit unit) {
    setProperty("font-size", value, unit);
  }

  public void setDisplay(final Display display) {
    setProperty("display", display == null ? "" : display.getCssName());
  }

  public String getProperty(final String name) {
    return element.getStyle().getPropertyValue(name);
  }

  public void setProperty(final String name, final String value) {
    element.getStyle().setProperty(name, value == null ? "" : value);
  }

  public void setProperty(final String name, final double value, final Unit unit) {
    setProperty(name, value + unit.suffix);
  }

  public void setTop(final double value, final Unit unit) {
    setProperty("top", value, unit);
  }

  public void setBottom(final double value, final Unit unit) {
    setProperty("bottom", value, unit);
  }

  public void setLeft(final double value, final Unit unit) {
    setProperty("left", value, unit);
  }

  public void setRight(final double value, final Unit unit) {
    setProperty("right", value, unit);
  }

  public void setWidth(final double value, final Unit unit) {
    setProperty("width", value, unit);
  }

  public void setHeight(final double value, final Unit unit) {
    setProperty("height", value, unit);
  }

  public void setZIndex(final int value) {
    setProperty("z-index", Integer.toString(value));
  }

  public void setPosition(final Position position) {
    setProperty("position", position == null ? "" : position.getCssName());
  }

  public void clearProperty(final String name) {
    setProperty(name, "");
  }

  /** CSS {@code position} keywords. */
  public enum Position implements HasCssName {
    STATIC("static"),
    RELATIVE("relative"),
    ABSOLUTE("absolute"),
    FIXED("fixed"),
    STICKY("sticky");

    private final String cssName;

    Position(final String cssName) {
      this.cssName = cssName;
    }

    @Override
    public String getCssName() {
      return cssName;
    }
  }

  public String getWidth() {
    return getProperty("width");
  }

  public String getHeight() {
    return getProperty("height");
  }

  public String getDisplay() {
    return getProperty("display");
  }

  public String getTop() {
    return getProperty("top");
  }

  public String getLeft() {
    return getProperty("left");
  }

  public String getZIndex() {
    return getProperty("z-index");
  }

  public void setWhiteSpace(final WhiteSpace whiteSpace) {
    setProperty("white-space", whiteSpace == null ? "" : whiteSpace.getCssName());
  }

  /** CSS {@code white-space} keywords. */
  public enum WhiteSpace implements HasCssName {
    NORMAL("normal"),
    NOWRAP("nowrap"),
    PRE("pre"),
    PRE_LINE("pre-line"),
    PRE_WRAP("pre-wrap");

    private final String cssName;

    WhiteSpace(final String cssName) {
      this.cssName = cssName;
    }

    @Override
    public String getCssName() {
      return cssName;
    }
  }

  public String getWhiteSpace() {
    return getProperty("white-space");
  }
}
