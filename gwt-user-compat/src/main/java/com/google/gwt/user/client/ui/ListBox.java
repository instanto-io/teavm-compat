/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * Copyright 2010 Google Inc.
 * %%
 * Reimplements, over TeaVM's JSO libraries, part of the GWT client API. Class,
 * method and package names follow GWT (https://github.com/gwtproject/gwt),
 * Copyright (C) The GWT Project Authors, licensed under the Apache License,
 * Version 2.0. Option text formatting retains the upstream implementation.
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
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.BidiFormatter;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.i18n.shared.HasDirectionEstimator;
import com.google.gwt.i18n.shared.WordCountDirectionEstimator;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.html.HTMLElement;

/** Select widget mirroring GWT's {@code ListBox} surface. */
public class ListBox extends FocusWidget
    implements HasChangeHandlers, HasName, HasDirectionEstimator {
  public static final DirectionEstimator DEFAULT_DIRECTION_ESTIMATOR =
      WordCountDirectionEstimator.get();
  private static final String BIDI_ATTR_NAME = "bidiwrapped";
  private DirectionEstimator estimator;

  public ListBox() {
    this(Document.get().createSelectElement());
    setStyleName("gwt-ListBox");
  }

  public ListBox(final boolean multipleSelect) {
    this();
    setMultipleSelect(multipleSelect);
  }

  protected ListBox(final Element element) {
    super(element);
  }

  public void addItem(final String item) {
    insertItem(item, -1);
  }

  public void addItem(final String item, final Direction direction) {
    insertItem(item, direction, -1);
  }

  public void addItem(final String item, final String value) {
    insertItem(item, value, -1);
  }

  public void addItem(final String item, final Direction direction, final String value) {
    insertItem(item, direction, value, -1);
  }

  public void insertItem(final String item, final int index) {
    insertItem(item, item, index);
  }

  public void insertItem(final String item, final Direction direction, final int index) {
    insertItem(item, direction, item, index);
  }

  public void insertItem(final String item, final String value, final int index) {
    insertItem(item, null, value, index);
  }

  public void insertItem(
      final String item, final Direction direction, final String value, final int index) {
    final OptionElement option = Document.get().createOptionElement();
    setOptionText(option, item, direction);
    option.setValue(value);
    final OptionElement before = index < 0 || index >= getItemCount() ? null : optionAt(index);
    SelectElement.as(getElement()).add(option, before);
  }

  public void removeItem(final int index) {
    optionAt(index).removeFromParent();
  }

  @Override
  public DirectionEstimator getDirectionEstimator() {
    return estimator;
  }

  @Override
  public void setDirectionEstimator(final DirectionEstimator directionEstimator) {
    estimator = directionEstimator;
  }

  @Override
  public void setDirectionEstimator(final boolean enabled) {
    setDirectionEstimator(enabled ? DEFAULT_DIRECTION_ESTIMATOR : null);
  }

  public void setItemText(final int index, final String text) {
    setItemText(index, text, null);
  }

  public void setItemText(final int index, final String text, final Direction direction) {
    final OptionElement option = optionAt(index);
    if (text == null) {
      throw new NullPointerException();
    }
    setOptionText(option, text, direction);
  }

  public String getSelectedItemText() {
    final int index = getSelectedIndex();
    return index == -1 ? null : getItemText(index);
  }

  public void clear() {
    getElement().setInnerHTML("");
  }

  public int getItemCount() {
    return optionCount(getElement().unwrap());
  }

  public String getItemText(final int index) {
    final Element option = optionAt(index);
    return getOptionText(OptionElement.as(option));
  }

  public String getValue(final int index) {
    final Element option = optionAt(index);
    return option.getPropertyString("value");
  }

  public void setValue(final int index, final String value) {
    final Element option = optionAt(index);
    if (option != null) {
      option.setPropertyString("value", value);
    }
  }

  public int getSelectedIndex() {
    return getElement().getPropertyInt("selectedIndex");
  }

  public void setSelectedIndex(final int index) {
    getElement().setPropertyInt("selectedIndex", index);
  }

  public String getSelectedValue() {
    final int index = getSelectedIndex();
    return index < 0 ? null : getValue(index);
  }

  public boolean isItemSelected(final int index) {
    final Element option = optionAt(index);
    return option.getPropertyBoolean("selected");
  }

  public void setItemSelected(final int index, final boolean selected) {
    final Element option = optionAt(index);
    if (option != null) {
      option.setPropertyBoolean("selected", selected);
    }
  }

  public boolean isMultipleSelect() {
    return getElement().getPropertyBoolean("multiple");
  }

  public void setMultipleSelect(final boolean multiple) {
    getElement().setPropertyBoolean("multiple", multiple);
  }

  public int getVisibleItemCount() {
    return getElement().getPropertyInt("size");
  }

  public void setVisibleItemCount(final int count) {
    getElement().setPropertyInt("size", count);
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

  protected String getOptionText(OptionElement option) {
    String text = option.getText();
    if (option.hasAttribute(BIDI_ATTR_NAME) && text.length() > 1) {
      text = text.substring(1, text.length() - 1);
    }
    return text;
  }

  protected void setOptionText(OptionElement option, String text, Direction dir) {
    if (dir == null && estimator != null) {
      dir = estimator.estimateDirection(text);
    }
    if (dir == null) {
      option.setText(text);
      option.removeAttribute(BIDI_ATTR_NAME);
    } else {
      String formattedText =
          BidiFormatter.getInstanceForCurrentLocale()
              .unicodeWrapWithKnownDir(dir, text, false /* isHtml */, false /* dirReset */);
      option.setText(formattedText);
      if (formattedText.length() > text.length()) {
        option.setAttribute(BIDI_ATTR_NAME, "");
      } else {
        option.removeAttribute(BIDI_ATTR_NAME);
      }
    }
  }

  private OptionElement optionAt(final int index) {
    if (index < 0 || index >= getItemCount()) {
      throw new IndexOutOfBoundsException();
    }
    final HTMLElement option = optionElementAt(getElement().unwrap(), index);
    return new OptionElement(option);
  }

  @JSBody(
      params = {"el"},
      script = "return el.options.length;")
  private static native int optionCount(HTMLElement el);

  @JSBody(
      params = {"el", "index"},
      script = "return el.options[index] || null;")
  private static native HTMLElement optionElementAt(HTMLElement el, int index);
}
