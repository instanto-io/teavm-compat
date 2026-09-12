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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.html.HTMLElement;

/** Select widget mirroring GWT's {@code ListBox} surface. */
public class ListBox extends FocusWidget implements HasChangeHandlers, HasName {

  public ListBox() {
    super(Document.get().createSelectElement());
  }

  public void addItem(final String item) {
    addItem(item, item);
  }

  public void addItem(final String item, final String value) {
    insertItem(item, value, getItemCount());
  }

  public void insertItem(final String item, final String value, final int index) {
    final Element option = Document.get().createOptionElement();
    option.setInnerText(item);
    option.setPropertyString("value", value == null ? item : value);
    final int count = getItemCount();
    if (index < 0 || index >= count) {
      getElement().appendChild(option);
    } else {
      getElement().insertBefore(option, optionAt(index));
    }
  }

  public void removeItem(final int index) {
    final Element option = optionAt(index);
    if (option != null) {
      option.removeFromParent();
    }
  }

  public void clear() {
    getElement().setInnerHTML("");
  }

  public int getItemCount() {
    return optionCount(getElement().unwrap());
  }

  public String getItemText(final int index) {
    final Element option = optionAt(index);
    return option == null ? null : option.getInnerText();
  }

  public String getValue(final int index) {
    final Element option = optionAt(index);
    return option == null ? null : option.getPropertyString("value");
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
    return option != null && option.getPropertyBoolean("selected");
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

  private Element optionAt(final int index) {
    final HTMLElement option = optionElementAt(getElement().unwrap(), index);
    return option == null ? null : new Element(option);
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
