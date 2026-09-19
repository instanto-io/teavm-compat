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

import com.google.gwt.core.client.JavaScriptObject;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;

public class Element extends com.google.gwt.dom.client.Node {
  private final HTMLElement element;
  private final Style style;

  public Element(final HTMLElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element must not be null");
    }
    this.element = element;
    this.style = new Style(element);
  }

  public HTMLElement unwrap() {
    return element;
  }

  public String getString() {
    return getPropertyString("outerHTML");
  }

  /** Narrows an opaque GWT JavaScript value to an element wrapper. */
  public static Element as(final JavaScriptObject value) {
    if (value == null) {
      return null;
    }
    if (!is(value)) {
      throw new IllegalArgumentException("value is not a DOM element");
    }
    return value instanceof Element ? (Element) value : wrap(value.unwrap().cast());
  }

  public static boolean is(final JavaScriptObject value) {
    return value != null && isElement(value.unwrap());
  }

  @JSBody(
      params = {"value"},
      script = "return !!value && value.nodeType === 1;")
  private static native boolean isElement(JSObject value);

  /** How far the element's content is scrolled from its left edge. */
  public int getScrollLeft() {
    return scrollLeft(element);
  }

  public void setScrollLeft(final int position) {
    setScrollLeft(element, position);
  }

  /** How far the element's content is scrolled from its top edge. */
  public int getScrollTop() {
    return scrollTop(element);
  }

  public void setScrollTop(final int position) {
    setScrollTop(element, position);
  }

  /** The full scrollable height, which is what a scroll position is measured against. */
  public int getScrollHeight() {
    return scrollHeight(element);
  }

  public int getScrollWidth() {
    return scrollWidth(element);
  }

  public int getClientWidth() {
    return element.getClientWidth();
  }

  public int getClientHeight() {
    return element.getClientHeight();
  }

  @JSBody(
      params = {"e"},
      script = "return e.scrollLeft | 0;")
  private static native int scrollLeft(HTMLElement e);

  @JSBody(
      params = {"e", "position"},
      script = "e.scrollLeft = position;")
  private static native void setScrollLeft(HTMLElement e, int position);

  @JSBody(
      params = {"e"},
      script = "return e.scrollTop | 0;")
  private static native int scrollTop(HTMLElement e);

  @JSBody(
      params = {"e", "position"},
      script = "e.scrollTop = position;")
  private static native void setScrollTop(HTMLElement e, int position);

  @JSBody(
      params = {"e"},
      script = "return e.scrollHeight | 0;")
  private static native int scrollHeight(HTMLElement e);

  @JSBody(
      params = {"e"},
      script = "return e.scrollWidth | 0;")
  private static native int scrollWidth(HTMLElement e);

  /** Chooses a typed wrapper while preserving the original browser node. */
  public static Element wrap(HTMLElement nativeElement) {
    if (nativeElement == null) return null;
    return switch (nativeElement.getTagName().toLowerCase()) {
      case "thead", "tbody", "tfoot" -> new TableSectionElement(nativeElement);
      case "tr" -> new TableRowElement(nativeElement);
      case "td", "th" -> new TableCellElement(nativeElement);
      case "select" -> new SelectElement(nativeElement);
      case "video" -> new VideoElement(nativeElement);
      case "canvas" -> new CanvasElement(nativeElement);
      case "div" -> new DivElement(nativeElement);
      case "optgroup" -> new OptGroupElement(nativeElement);
      case "label" -> new LabelElement(nativeElement);
      case "span" -> new SpanElement(nativeElement);
      case "option" -> new OptionElement(nativeElement);
      case "img" -> new ImageElement(nativeElement);
      case "source" -> new SourceElement(nativeElement);
      case "body" -> new BodyElement(nativeElement);
      case "button" -> new ButtonElement(nativeElement);
      case "link" -> new LinkElement(nativeElement);
      case "meta" -> new MetaElement(nativeElement);
      case "input" -> new InputElement(nativeElement);
      case "head" -> new HeadElement(nativeElement);
      case "form" -> new FormElement(nativeElement);
      case "a" -> new AnchorElement(nativeElement);
      case "p" -> new ParagraphElement(nativeElement);
      case "iframe" -> new IFrameElement(nativeElement);
      case "textarea" -> new TextAreaElement(nativeElement);
      case "style" -> new StyleElement(nativeElement);
      default -> new Element(nativeElement);
    };
  }

  public void setPropertyDouble(String name, double value) {
    propertyDouble(unwrap(), name, value);
  }

  @JSBody(
      params = {"element", "name", "value"},
      script = "element[name]=value;")
  private static native void propertyDouble(HTMLElement element, String name, double value);

  public void setPropertyObject(String name, Object value) {
    if (value instanceof Boolean) {
      setPropertyBoolean(name, (Boolean) value);
      return;
    }
    if (value instanceof String) {
      setPropertyString(name, (String) value);
      return;
    }
    if (value instanceof Number) {
      setPropertyDouble(name, ((Number) value).doubleValue());
      return;
    }
    Object raw =
        value instanceof com.google.gwt.core.client.JavaScriptObject
            ? ((com.google.gwt.core.client.JavaScriptObject) value).unwrap()
            : value;
    propertyObject(unwrap(), name, raw);
  }

  @JSBody(
      params = {"element", "name", "value"},
      script = "element[name] = value;")
  private static native void propertyObject(HTMLElement element, String name, Object value);

  public Style getStyle() {

    return style;
  }

  public void setInnerText(final String text) {
    element.setTextContent(text == null ? "" : text);
  }

  public String getInnerText() {
    return element.getTextContent();
  }

  public void setInnerHTML(final String html) {
    element.setInnerHTML(html == null ? "" : html);
  }

  public String getInnerHTML() {
    return element.getInnerHTML();
  }

  public void setAttribute(final String name, final String value) {
    element.setAttribute(name, value == null ? "" : value);
  }

  public String getAttribute(final String name) {
    String value = element.getAttribute(name);
    return value == null ? "" : value;
  }

  public void removeAttribute(final String name) {
    element.removeAttribute(name);
  }

  /**
   * Sets a live DOM property. Properties such as {@code checked} and {@code value} must not be
   * written as attributes: the attribute only seeds the initial value, so attribute writes are
   * ignored once the user has interacted with the control.
   */
  public void setPropertyBoolean(final String name, final boolean value) {
    setBooleanProperty(element, name, value);
  }

  public boolean getPropertyBoolean(final String name) {
    return getBooleanProperty(element, name);
  }

  public void setPropertyString(final String name, final String value) {
    setStringProperty(element, name, value == null ? "" : value);
  }

  public String getPropertyString(final String name) {
    return getStringProperty(element, name);
  }

  public void setPropertyInt(final String name, final int value) {
    setIntProperty(element, name, value);
  }

  public int getPropertyInt(final String name) {
    return getIntProperty(element, name);
  }

  @JSBody(
      params = {"el", "name", "value"},
      script = "el[name] = value;")
  private static native void setBooleanProperty(HTMLElement el, String name, boolean value);

  @JSBody(
      params = {"el", "name"},
      script = "return !!el[name];")
  private static native boolean getBooleanProperty(HTMLElement el, String name);

  @JSBody(
      params = {"el", "name", "value"},
      script = "el[name] = value;")
  private static native void setStringProperty(HTMLElement el, String name, String value);

  @JSBody(
      params = {"el", "name"},
      script = "var v = el[name]; return v == null ? null : String(v);")
  private static native String getStringProperty(HTMLElement el, String name);

  @JSBody(
      params = {"el", "name", "value"},
      script = "el[name] = value;")
  private static native void setIntProperty(HTMLElement el, String name, int value);

  @JSBody(
      params = {"el", "name"},
      script = "var v = el[name]; return v == null ? 0 : v | 0;")
  private static native int getIntProperty(HTMLElement el, String name);

  public boolean hasAttribute(final String name) {
    return element.hasAttribute(name);
  }

  public Element getFirstChildElement() {
    final org.teavm.jso.dom.html.HTMLElement first = firstElementChild(element);
    return first == null ? null : new Element(first);
  }

  @JSBody(
      params = {"el"},
      script = "return el.firstElementChild;")
  private static native HTMLElement firstElementChild(HTMLElement el);

  public Element getParentElement() {
    final HTMLElement parent = parentElement(element);
    return parent == null ? null : new Element(parent);
  }

  @JSBody(
      params = {"el"},
      script = "return el.parentElement;")
  private static native HTMLElement parentElement(HTMLElement el);

  public void focus() {
    element.focus();
  }

  public void blur() {
    element.blur();
  }

  public String getTagName() {
    return element.getTagName();
  }

  public void setId(final String id) {
    element.setId(id == null ? "" : id);
  }

  public String getId() {
    return element.getId();
  }

  public void setClassName(final String className) {
    element.setClassName(className == null ? "" : className);
  }

  public String getClassName() {
    return element.getClassName();
  }

  public void addClassName(final String className) {
    if (className != null && !className.isEmpty()) {
      element.getClassList().add(className);
    }
  }

  public void removeClassName(final String className) {
    if (className != null && !className.isEmpty()) {
      element.getClassList().remove(className);
    }
  }

  public boolean hasClassName(final String className) {
    return className != null && element.getClassList().contains(className);
  }

  public void appendChild(final Element child) {
    element.appendChild((Node) child.unwrap());
  }

  public void insertBefore(final Element child, final Element before) {
    element.insertBefore((Node) child.unwrap(), before == null ? null : (Node) before.unwrap());
  }

  public void insertFirst(final Element child) {
    final Node firstChild = element.getFirstChild();
    if (firstChild == null) {
      appendChild(child);
    } else {
      element.insertBefore((Node) child.unwrap(), firstChild);
    }
  }

  public void removeFromParent() {
    final Node parent = element.getParentNode();
    if (parent != null) {
      parent.removeChild((Node) element);
    }
  }

  public void setInnerSafeHtml(final com.google.gwt.safehtml.shared.SafeHtml html) {
    setInnerHTML(html == null ? "" : html.asString());
  }

  public boolean hasParentElement() {
    return parentElement(element) != null;
  }

  public boolean isOrHasChild(final Element child) {
    return child != null && contains(element, child.unwrap());
  }

  public void removeChild(final Element child) {
    if (child != null) {
      element.removeChild((org.teavm.jso.dom.xml.Node) child.unwrap());
    }
  }

  public void removeAllChildren() {
    setInnerHTML("");
  }

  public void scrollIntoView() {
    scrollTo(element);
  }

  @JSBody(
      params = {"el", "child"},
      script = "return el === child || el.contains(child);")
  private static native boolean contains(HTMLElement el, HTMLElement child);

  @JSBody(
      params = {"el"},
      script = "el.scrollIntoView();")
  private static native void scrollTo(HTMLElement el);

  public int getTabIndex() {
    return getPropertyInt("tabIndex");
  }

  public void setTabIndex(final int index) {
    setPropertyInt("tabIndex", index);
  }

  public void setAccessKey(final String key) {
    if (key == null || key.isEmpty()) {
      removeAttribute("accesskey");
    } else {
      setAttribute("accesskey", key);
    }
  }

  public String getAccessKey() {
    return getAttribute("accesskey");
  }

  /** Inserts {@code child} directly after {@code after}, or first when it is null. */
  public void insertAfter(final Element child, final Element after) {
    if (after == null) {
      insertFirst(child);
      return;
    }
    final org.teavm.jso.dom.html.HTMLElement next = nextElementSibling(after.unwrap());
    if (next == null) {
      appendChild(child);
    } else {
      element.insertBefore((org.teavm.jso.dom.xml.Node) child.unwrap(), next);
    }
  }

  public int getAbsoluteLeft() {
    return boundingLeft(element);
  }

  public int getAbsoluteTop() {
    return boundingTop(element);
  }

  public int getOffsetWidth() {
    return offsetWidth(element);
  }

  public int getOffsetTop() {
    return getPropertyInt("offsetTop");
  }

  public int getOffsetHeight() {
    return offsetHeight(element);
  }

  @JSBody(
      params = {"el"},
      script = "return el.nextElementSibling;")
  private static native HTMLElement nextElementSibling(HTMLElement el);

  @JSBody(
      params = {"el"},
      script = "return Math.round(el.getBoundingClientRect().left + window.pageXOffset);")
  private static native int boundingLeft(HTMLElement el);

  @JSBody(
      params = {"el"},
      script = "return Math.round(el.getBoundingClientRect().top + window.pageYOffset);")
  private static native int boundingTop(HTMLElement el);

  @JSBody(
      params = {"el"},
      script = "return el.offsetWidth | 0;")
  private static native int offsetWidth(HTMLElement el);

  @JSBody(
      params = {"el"},
      script = "return el.offsetHeight | 0;")
  private static native int offsetHeight(HTMLElement el);

  public Element getNextSiblingElement() {
    final HTMLElement next = nextElementSibling(element);
    return next == null ? null : new Element(next);
  }

  public int getAbsoluteRight() {
    return getAbsoluteLeft() + getOffsetWidth();
  }

  public int getAbsoluteBottom() {
    return getAbsoluteTop() + getOffsetHeight();
  }

  /** Appends a text node, as GWT's overload does. */
  public void appendChild(final Text text) {
    if (text != null) {
      element.appendChild(text.unwrap());
    }
  }

  public void insertBefore(final Text text, final Element before) {
    if (text == null) {
      return;
    }
    if (before == null) {
      element.appendChild(text.unwrap());
    } else {
      element.insertBefore(text.unwrap(), (org.teavm.jso.dom.xml.Node) before.unwrap());
    }
  }

  /** The descendants of this element with the given tag name, in document order. */
  public NodeList<Element> getElementsByTagName(final String tagName) {
    return new NodeList<Element>(rawElementsByTagName(element, tagName));
  }

  /** The first descendant matching {@code selectors}, or null. */
  public Element querySelector(final String selectors) {
    final HTMLElement found = rawQuerySelector(element, selectors);
    return found == null ? null : new Element(found);
  }

  /** Every descendant matching {@code selectors}, in document order. */
  public NodeList<Element> querySelectorAll(final String selectors) {
    return new NodeList<Element>(rawQuerySelectorAll(element, selectors));
  }

  @JSBody(
      params = {"el", "tagName"},
      script = "return el.getElementsByTagName(tagName);")
  private static native JSObject rawElementsByTagName(HTMLElement el, String tagName);

  @JSBody(
      params = {"el", "selectors"},
      script = "return el.querySelector(selectors);")
  private static native HTMLElement rawQuerySelector(HTMLElement el, String selectors);

  @JSBody(
      params = {"el", "selectors"},
      script = "return el.querySelectorAll(selectors);")
  private static native JSObject rawQuerySelectorAll(HTMLElement el, String selectors);
}
