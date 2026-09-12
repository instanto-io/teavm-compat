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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.text.shared.testing.PassthroughRenderer;
import com.google.gwt.view.client.ProvidesKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A select whose options are typed values rendered to display strings. */
public class ValueListBox<T> extends Composite implements HasValue<T>, HasName {

  private final ListBox listBox = new ListBox();
  private final List<T> values = new ArrayList<>();
  private final Map<Object, Integer> valueKeyToIndex = new LinkedHashMap<>();
  private final Renderer<? super T> renderer;
  private final ProvidesKey<T> keyProvider;

  @SuppressWarnings("unchecked")
  public ValueListBox() {
    this((Renderer<? super T>) PassthroughRenderer.instance());
  }

  public ValueListBox(final Renderer<? super T> renderer) {
    this(renderer, item -> item);
  }

  public ValueListBox(final Renderer<? super T> renderer, final ProvidesKey<T> keyProvider) {
    this.renderer = renderer;
    this.keyProvider = keyProvider;
    initWidget(listBox);
    listBox.addChangeHandler(event -> ValueChangeEvent.fire(this, getValue()));
  }

  /** Replaces the option list, preserving the current value if it is still present. */
  public void setAcceptableValues(final Iterable<T> newValues) {
    final T current = getValue();
    values.clear();
    valueKeyToIndex.clear();
    listBox.clear();
    for (final T value : newValues) {
      addValue(value);
    }
    if (current != null && valueKeyToIndex.containsKey(keyOf(current))) {
      setValue(current, false);
    }
  }

  private void addValue(final T value) {
    final Object key = keyOf(value);
    if (valueKeyToIndex.containsKey(key)) {
      throw new IllegalArgumentException("Duplicate value: " + value);
    }
    valueKeyToIndex.put(key, values.size());
    values.add(value);
    listBox.addItem(renderer.render(value));
  }

  private Object keyOf(final T value) {
    return keyProvider == null ? value : keyProvider.getKey(value);
  }

  @Override
  public T getValue() {
    final int index = listBox.getSelectedIndex();
    return index < 0 || index >= values.size() ? null : values.get(index);
  }

  @Override
  public void setValue(final T value) {
    setValue(value, false);
  }

  @Override
  public void setValue(final T value, final boolean fireEvents) {
    final T oldValue = getValue();
    if (value == null) {
      listBox.setSelectedIndex(-1);
    } else {
      final Integer index = valueKeyToIndex.get(keyOf(value));
      if (index == null) {
        addValue(value);
        listBox.setSelectedIndex(values.size() - 1);
      } else {
        listBox.setSelectedIndex(index);
      }
    }
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldValue, getValue());
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.<T>getType());
  }

  @Override
  public String getName() {
    return listBox.getName();
  }

  @Override
  public void setName(final String name) {
    listBox.setName(name);
  }
}
