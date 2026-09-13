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
    INLINE_BLOCK("inline-block"),
    INLINE_TABLE("inline-table"),
    LIST_ITEM("list-item"),
    RUN_IN("run-in"),
    TABLE("table"),
    TABLE_CAPTION("table-caption"),
    TABLE_COLUMN_GROUP("table-column-group"),
    TABLE_HEADER_GROUP("table-header-group"),
    TABLE_FOOTER_GROUP("table-footer-group"),
    TABLE_ROW_GROUP("table-row-group"),
    TABLE_CELL("table-cell"),
    TABLE_COLUMN("table-column"),
    TABLE_ROW("table-row"),
    INITIAL("initial"),
    FLEX("flex"),
    INLINE_FLEX("inline-flex");

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
    return element.getStyle().getPropertyValue(cssProperty(name));
  }

  public void setProperty(final String name, final String value) {
    element.getStyle().setProperty(cssProperty(name), value == null ? "" : value);
  }

  private static String cssProperty(String name) {
    if (name.startsWith("--")) return name;
    if (name.equals("cssFloat") || name.equals("styleFloat")) return "float";
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c >= 'A' && c <= 'Z') result.append('-').append((char) (c + 'a' - 'A'));
      else result.append(c);
    }
    return result.toString();
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

  public enum Float implements HasCssName {
    LEFT("left"),
    RIGHT("right"),
    NONE("none");
    private final String cssName;

    Float(String cssName) {
      this.cssName = cssName;
    }

    public String getCssName() {
      return cssName;
    }
  }

  public void setFloat(Float value) {
    setProperty("float", value.getCssName());
  }

  public enum FontWeight implements HasCssName {
    NORMAL("normal"),
    BOLD("bold"),
    BOLDER("bolder"),
    LIGHTER("lighter");
    private final String cssName;

    FontWeight(String cssName) {
      this.cssName = cssName;
    }

    public String getCssName() {
      return cssName;
    }
  }

  public void setFontWeight(FontWeight value) {
    setProperty("font-weight", value.getCssName());
  }

  public enum VerticalAlign implements HasCssName {
    BASELINE("baseline"),
    SUB("sub"),
    SUPER("super"),
    TOP("top"),
    TEXT_TOP("text-top"),
    MIDDLE("middle"),
    BOTTOM("bottom"),
    TEXT_BOTTOM("text-bottom");
    private final String cssName;

    VerticalAlign(String cssName) {
      this.cssName = cssName;
    }

    public String getCssName() {
      return cssName;
    }
  }

  public void setVerticalAlign(VerticalAlign value) {
    setProperty("vertical-align", value.getCssName());
  }

  public enum Visibility implements HasCssName {
    VISIBLE("visible"),
    HIDDEN("hidden");
    private final String cssName;

    Visibility(String cssName) {
      this.cssName = cssName;
    }

    public String getCssName() {
      return cssName;
    }
  }

  public void setVisibility(Visibility value) {
    setProperty("visibility", value.getCssName());
  }

  public enum Overflow implements HasCssName {
    VISIBLE("visible"),
    HIDDEN("hidden"),
    SCROLL("scroll"),
    AUTO("auto");
    private final String cssName;

    Overflow(String cssName) {
      this.cssName = cssName;
    }

    public String getCssName() {
      return cssName;
    }
  }

  public void setOverflow(Overflow value) {
    setProperty("overflow", value.getCssName());
  }

  public enum Cursor implements HasCssName {
    DEFAULT("default"),
    AUTO("auto"),
    CROSSHAIR("crosshair"),
    POINTER("pointer"),
    MOVE("move"),
    E_RESIZE("e-resize"),
    NE_RESIZE("ne-resize"),
    NW_RESIZE("nw-resize"),
    N_RESIZE("n-resize"),
    SE_RESIZE("se-resize"),
    SW_RESIZE("sw-resize"),
    S_RESIZE("s-resize"),
    W_RESIZE("w-resize"),
    TEXT("text"),
    WAIT("wait"),
    HELP("help"),
    COL_RESIZE("col-resize"),
    ROW_RESIZE("row-resize");
    private final String cssName;

    Cursor(String cssName) {
      this.cssName = cssName;
    }

    public String getCssName() {
      return cssName;
    }
  }

  public void setCursor(Cursor value) {
    setProperty("cursor", value.getCssName());
  }

  public void clearFloat() {
    clearProperty("float");
  }

  public String getFontWeight() {
    return getProperty("font-weight");
  }

  public void clearFontWeight() {
    clearProperty("font-weight");
  }

  public String getVerticalAlign() {
    return getProperty("vertical-align");
  }

  public void clearVerticalAlign() {
    clearProperty("vertical-align");
  }

  public String getVisibility() {
    return getProperty("visibility");
  }

  public void clearVisibility() {
    clearProperty("visibility");
  }

  public String getOverflow() {
    return getProperty("overflow");
  }

  public void clearOverflow() {
    clearProperty("overflow");
  }

  public String getCursor() {
    return getProperty("cursor");
  }

  public void clearCursor() {
    clearProperty("cursor");
  }

  public String getOpacity() {
    return getProperty("opacity");
  }

  public void clearOpacity() {
    clearProperty("opacity");
  }

  public String getPosition() {
    return getProperty("position");
  }

  public void clearPosition() {
    clearProperty("position");
  }

  public String getFontSize() {
    return getProperty("font-size");
  }

  public void clearFontSize() {
    clearProperty("font-size");
  }

  public String getMargin() {
    return getProperty("margin");
  }

  public void clearMargin() {
    clearProperty("margin");
  }

  public String getPadding() {
    return getProperty("padding");
  }

  public void clearPadding() {
    clearProperty("padding");
  }

  public String getLineHeight() {
    return getProperty("line-height");
  }

  public void clearLineHeight() {
    clearProperty("line-height");
  }

  public String getBackgroundColor() {
    return getProperty("background-color");
  }

  public void clearBackgroundColor() {
    clearProperty("background-color");
  }

  public void clearDisplay() {
    clearProperty("display");
  }

  public void setMargin(double value, Unit unit) {
    setProperty("margin", value, unit);
  }

  public void setPadding(double value, Unit unit) {
    setProperty("padding", value, unit);
  }

  public void setLineHeight(double value, Unit unit) {
    setProperty("line-height", value, unit);
  }

  public void setOpacity(double value) {
    setProperty("opacity", Double.toString(value));
  }

  public void setBackgroundColor(String value) {
    setProperty("background-color", value);
  }
}
