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

/** Base for widgets that can take focus and be disabled. */
public class FocusWidget extends Widget implements HasEnabled, Focusable {

  protected FocusWidget() {}

  protected FocusWidget(final Element element) {
    setElement(element);
  }

  @Override
  public boolean isEnabled() {
    return !getElement().getPropertyBoolean("disabled");
  }

  @Override
  public void setEnabled(final boolean enabled) {
    getElement().setPropertyBoolean("disabled", !enabled);
  }

  @Override
  public int getTabIndex() {
    return getElement().getPropertyInt("tabIndex");
  }

  @Override
  public void setTabIndex(final int index) {
    getElement().setPropertyInt("tabIndex", index);
  }

  @Override
  public void setAccessKey(final char key) {
    getElement().setAttribute("accesskey", String.valueOf(key));
  }

  @Override
  public void setFocus(final boolean focused) {
    if (focused) {
      getElement().focus();
    } else {
      getElement().blur();
    }
  }

  public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(
      final com.google.gwt.event.dom.client.BlurHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.BlurEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(
      final com.google.gwt.event.dom.client.FocusHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.FocusEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addKeyDownHandler(
      final com.google.gwt.event.dom.client.KeyDownHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.KeyDownEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addKeyUpHandler(
      final com.google.gwt.event.dom.client.KeyUpHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.KeyUpEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addKeyPressHandler(
      final com.google.gwt.event.dom.client.KeyPressHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.KeyPressEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseDownHandler(
      final com.google.gwt.event.dom.client.MouseDownHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseDownEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseUpHandler(
      final com.google.gwt.event.dom.client.MouseUpHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseUpEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseOverHandler(
      final com.google.gwt.event.dom.client.MouseOverHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseOverEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseOutHandler(
      final com.google.gwt.event.dom.client.MouseOutHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseOutEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseMoveHandler(
      final com.google.gwt.event.dom.client.MouseMoveHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.MouseMoveEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addMouseWheelHandler(
      final com.google.gwt.event.dom.client.MouseWheelHandler handler) {
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

  public com.google.gwt.event.shared.HandlerRegistration addGestureStartHandler(
      com.google.gwt.event.dom.client.GestureStartHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.GestureStartEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addGestureChangeHandler(
      com.google.gwt.event.dom.client.GestureChangeHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.GestureChangeEvent.getType());
  }

  public com.google.gwt.event.shared.HandlerRegistration addGestureEndHandler(
      com.google.gwt.event.dom.client.GestureEndHandler handler) {
    return addDomHandler(handler, com.google.gwt.event.dom.client.GestureEndEvent.getType());
  }
}
