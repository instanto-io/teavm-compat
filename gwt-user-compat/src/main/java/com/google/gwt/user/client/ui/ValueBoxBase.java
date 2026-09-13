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
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.AutoDirectionHandler;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.i18n.shared.HasDirectionEstimator;
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
    implements HasValue<T>,
        HasName,
        HasChangeHandlers,
        HasText,
        AutoDirectionHandler.Target,
        HasDirectionEstimator,
        IsEditor<ValueBoxEditor<T>> {

  public enum TextAlignment {
    CENTER,
    JUSTIFY,
    LEFT,
    RIGHT
  }

  private final AutoDirectionHandler directionHandler;
  private ValueBoxEditor<T> editor;
  private com.google.gwt.user.client.Event currentKeyEvent;

  private static final TextBoxImpl IMPL = new TextBoxImpl();

  private final Renderer<T> renderer;
  private final Parser<T> parser;
  private boolean valueChangeBridged;

  protected ValueBoxBase(
      final Element element, final Renderer<T> renderer, final Parser<T> parser) {
    super(element);
    this.renderer = renderer;
    this.parser = parser;
    directionHandler = AutoDirectionHandler.addTo(this, false);
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
    directionHandler.refreshDirection();
  }

  @Override
  public T getValue() {
    try {
      return getValueOrThrow();
    } catch (final ParseException e) {
      return null;
    }
  }

  public T getValueOrThrow() throws ParseException {
    String text = getText();
    T parsed = parser.parse(text);
    return text.isEmpty() ? null : parsed;
  }

  public ValueBoxEditor<T> asEditor() {
    if (editor == null) editor = ValueBoxEditor.of(this);
    return editor;
  }

  public Direction getDirection() {
    String direction = getElement().getAttribute("dir");
    return "rtl".equalsIgnoreCase(direction)
        ? Direction.RTL
        : "ltr".equalsIgnoreCase(direction) ? Direction.LTR : Direction.DEFAULT;
  }

  public void setDirection(Direction direction) {
    if (direction == Direction.DEFAULT) getElement().removeAttribute("dir");
    else getElement().setAttribute("dir", direction == Direction.RTL ? "rtl" : "ltr");
  }

  public DirectionEstimator getDirectionEstimator() {
    return directionHandler.getDirectionEstimator();
  }

  public void setDirectionEstimator(boolean enabled) {
    directionHandler.setDirectionEstimator(enabled);
  }

  public void setDirectionEstimator(DirectionEstimator estimator) {
    directionHandler.setDirectionEstimator(estimator);
  }

  public void setAlignment(TextAlignment alignment) {
    getElement()
        .getStyle()
        .setProperty("textAlign", alignment.name().toLowerCase(java.util.Locale.ROOT));
  }

  public String getSelectedText() {
    int start = getCursorPos();
    return getText().substring(start, start + getSelectionLength());
  }

  public void setCursorPos(int position) {
    setSelectionRange(position, 0);
  }

  public void cancelKey() {
    if (currentKeyEvent != null) currentKeyEvent.preventDefault();
  }

  @Override
  public void onBrowserEvent(com.google.gwt.user.client.Event event) {
    com.google.gwt.user.client.Event previous = currentKeyEvent;
    if (event.getType().equals("keydown")
        || event.getType().equals("keyup")
        || event.getType().equals("keypress")) currentKeyEvent = event;
    try {
      super.onBrowserEvent(event);
    } finally {
      currentKeyEvent = previous;
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    directionHandler.refreshDirection();
  }

  @Override
  public void setValue(final T value) {
    setValue(value, false);
  }

  @Override
  public void setValue(final T value, final boolean fireEvents) {
    final T oldValue = fireEvents ? getValue() : null;
    setText(renderer == null ? String.valueOf(value) : renderer.render(value));
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
    if (!isAttached()) return;
    if (pos < 0 || length < 0 || pos > getText().length() - length)
      throw new IndexOutOfBoundsException();
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
