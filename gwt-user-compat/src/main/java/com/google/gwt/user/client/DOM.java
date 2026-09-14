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
package com.google.gwt.user.client;

import com.google.gwt.dom.client.Document;

/** Element creation and low-level DOM helpers. */
public final class DOM {

  private DOM() {}

  public static Element createFieldSet() {
    return Element.as(Document.get().createFieldSetElement());
  }

  public static void appendChild(
      final com.google.gwt.dom.client.Element parent,
      final com.google.gwt.dom.client.Element child) {
    parent.appendChild(child);
  }

  public static void insertChild(
      final com.google.gwt.dom.client.Element parent,
      final com.google.gwt.dom.client.Element child,
      final int index) {
    parent.insertBefore(child, childAt(parent, index));
  }

  public static void removeChild(
      final com.google.gwt.dom.client.Element parent,
      final com.google.gwt.dom.client.Element child) {
    parent.removeChild(child);
  }

  public static Element createLabel() {
    return Element.as(Document.get().createElement("label"));
  }

  public static Element createElement(String tag) {
    return Element.as(Document.get().createElement(tag));
  }

  public static Element getChild(com.google.gwt.dom.client.Element parent, int index) {
    return Element.as(childAt(parent, index));
  }

  public static Element createDiv() {
    return Element.as(Document.get().createDivElement());
  }

  public static Element createSpan() {
    return Element.as(Document.get().createSpanElement());
  }

  public static Element createAnchor() {
    return Element.as(Document.get().createAnchorElement());
  }

  public static Element createInputCheck() {
    return Element.as(Document.get().createCheckInputElement());
  }

  public static Element createInputRadio(final String name) {
    return Element.as(Document.get().createRadioInputElement(name));
  }

  public static Element createButton() {
    return Element.as(Document.get().createPushButtonElement());
  }

  public static Element createTable() {
    return Element.as(Document.get().createTableElement());
  }

  public static Element getElementById(final String id) {
    return Element.as(Document.get().getElementById(id));
  }

  public static String createUniqueId() {
    return Document.get().createUniqueId();
  }

  /**
   * The event mask for a native event. GWT uses these bits to route events through a central
   * dispatcher; handlers here bind DOM listeners directly, so this reports the event only by name
   * and returns {@code UNDEFINED} for anything unmapped.
   */
  public static int eventGetType(final Event event) {
    if (event == null) {
      return Event.UNDEFINED;
    }
    switch (event.getType()) {
      case "click":
        return Event.ONCLICK;
      case "dblclick":
        return Event.ONDBLCLICK;
      case "change":
        return Event.ONCHANGE;
      case "focus":
        return Event.ONFOCUS;
      case "blur":
        return Event.ONBLUR;
      case "keydown":
        return Event.ONKEYDOWN;
      case "keypress":
        return Event.ONKEYPRESS;
      case "keyup":
        return Event.ONKEYUP;
      case "mousedown":
        return Event.ONMOUSEDOWN;
      case "mouseup":
        return Event.ONMOUSEUP;
      case "mousemove":
        return Event.ONMOUSEMOVE;
      case "mouseover":
        return Event.ONMOUSEOVER;
      case "mouseout":
        return Event.ONMOUSEOUT;
      default:
        return Event.UNDEFINED;
    }
  }

  /**
   * No-op. GWT registers an element with its central event dispatcher here; this layer binds
   * listeners when a handler is added instead.
   */
  public static void setEventListener(
      final com.google.gwt.dom.client.Element element, final Object listener) {}

  public static void sinkEvents(
      final com.google.gwt.dom.client.Element element, final int eventBits) {}

  private static com.google.gwt.dom.client.Element childAt(
      final com.google.gwt.dom.client.Element parent, final int index) {
    com.google.gwt.dom.client.Element child = parent.getFirstChildElement();
    for (int i = 0; i < index && child != null; i++) {
      child = child.getNextSiblingElement();
    }
    return child;
  }

  public static Element createTR() {
    return createElement("tr");
  }

  public static Element createTD() {
    return createElement("td");
  }

  public static Element createTH() {
    return createElement("th");
  }
}
