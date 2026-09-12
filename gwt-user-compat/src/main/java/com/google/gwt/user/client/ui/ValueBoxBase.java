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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.impl.TextBoxImpl;
import java.text.ParseException;

/**
 * Base for text-entry widgets holding a typed value.
 *
 * <p>The value is rendered into and parsed out of the input's text with the supplied {@link
 * Renderer} and {@link Parser}. Value-change events fire on the browser's {@code change} event, so
 * a handler sees one event per committed edit rather than one per keystroke, matching GWT.
 */
public class ValueBoxBase<T> extends FocusWidget
    implements HasValue<T>, HasName, HasChangeHandlers {

  private static final TextBoxImpl IMPL = new TextBoxImpl();

  private final Renderer<T> renderer;
  private final Parser<T> parser;
  private boolean valueChangeBridged;

  protected ValueBoxBase(
      final Element element, final Renderer<T> renderer, final Parser<T> parser) {
    super(element);
    this.renderer = renderer;
    this.parser = parser;
  }

  protected TextBoxImpl getImpl() {
    return IMPL;
  }

  public String getValueAsString() {
    return getElement().getPropertyString("value");
  }

  protected void setValueAsString(final String value) {
    getElement().setPropertyString("value", value == null ? "" : value);
  }

  public String getText() {
    return getValueAsString();
  }

  public void setText(final String text) {
    setValueAsString(text);
  }

  @Override
  public T getValue() {
    final String text = getValueAsString();
    if (parser == null) {
      return null;
    }
    try {
      return parser.parse(text);
    } catch (final ParseException e) {
      return null;
    }
  }

  @Override
  public void setValue(final T value) {
    setValue(value, false);
  }

  @Override
  public void setValue(final T value, final boolean fireEvents) {
    final T oldValue = fireEvents ? getValue() : null;
    setValueAsString(renderer == null ? String.valueOf(value) : renderer.render(value));
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldValue, getValue());
    }
  }

  public int getCursorPos() {
    return getImpl().getCursorPos(getElement());
  }

  public int getSelectionLength() {
    return getImpl().getSelectionLength(getElement());
  }

  public void setSelectionRange(final int pos, final int length) {
    getImpl().setSelectionRange(getElement(), pos, length);
  }

  public void selectAll() {
    setSelectionRange(0, getValueAsString().length());
  }

  public boolean isReadOnly() {
    return getElement().getPropertyBoolean("readOnly");
  }

  public void setReadOnly(final boolean readOnly) {
    getElement().setPropertyBoolean("readOnly", readOnly);
  }

  @Override
  public String getName() {
    return getElement().getPropertyString("name");
  }

  @Override
  public void setName(final String name) {
    getElement().setPropertyString("name", name);
  }

  @Override
  public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  public HandlerRegistration addKeyUpHandler(final KeyUpHandler handler) {
    return addDomHandler(handler, KeyUpEvent.getType());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
    bridgeValueChange();
    return addHandler(handler, ValueChangeEvent.<T>getType());
  }

  /** Bridges the browser {@code change} event onto the logical value-change event. */
  private void bridgeValueChange() {
    if (valueChangeBridged) {
      return;
    }
    valueChangeBridged = true;
    addChangeHandler(event -> ValueChangeEvent.fire(this, getValue()));
  }
}
