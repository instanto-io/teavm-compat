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
import com.google.gwt.dom.client.Element;

/** Text-only widget rendered as a {@code div}, matching GWT's {@code Label}. */
public class Label extends Widget
    implements HasText,
        com.google.gwt.event.dom.client.HasAllMouseHandlers,
        com.google.gwt.event.dom.client.HasAllTouchHandlers {

  public Label() {
    this(Document.get().createDivElement());
  }

  public Label(final String text) {
    this();
    setText(text);
  }

  protected Label(final Element element) {
    setElement(element);
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(final String text) {
    getElement().setInnerText(text);
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseDownHandler(
      com.google.gwt.event.dom.client.MouseDownHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseDownEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseUpHandler(
      com.google.gwt.event.dom.client.MouseUpHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseUpEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseMoveHandler(
      com.google.gwt.event.dom.client.MouseMoveHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseMoveEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseOverHandler(
      com.google.gwt.event.dom.client.MouseOverHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseOverEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseOutHandler(
      com.google.gwt.event.dom.client.MouseOutHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseOutEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseWheelHandler(
      com.google.gwt.event.dom.client.MouseWheelHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseWheelEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addTouchStartHandler(
      com.google.gwt.event.dom.client.TouchStartHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.TouchStartEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addTouchMoveHandler(
      com.google.gwt.event.dom.client.TouchMoveHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.TouchMoveEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addTouchEndHandler(
      com.google.gwt.event.dom.client.TouchEndHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.TouchEndEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addTouchCancelHandler(
      com.google.gwt.event.dom.client.TouchCancelHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.TouchCancelEvent.getType());
  }
}
