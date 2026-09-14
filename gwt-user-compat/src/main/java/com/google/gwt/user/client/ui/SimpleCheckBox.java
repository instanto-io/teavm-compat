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
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/** A checkbox with no label of its own. */
public class SimpleCheckBox extends FocusWidget implements HasName, HasValue<Boolean> {

  private boolean valueChangeHandlerInitialized;

  public SimpleCheckBox() {
    this(Document.get().createCheckInputElement());
  }

  protected SimpleCheckBox(final Element element) {
    super(element);
  }

  protected InputElement getInputElement() {
    return InputElement.as(getElement());
  }

  @Override
  public Boolean getValue() {
    return getInputElement().isChecked();
  }

  @Override
  public void setValue(final Boolean value) {
    setValue(value, false);
  }

  @Override
  public void setValue(final Boolean value, final boolean fireEvents) {
    final boolean effective = value != null && value;
    final Boolean old = fireEvents ? getValue() : null;
    getInputElement().setChecked(effective);
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, old, effective);
    }
  }

  public boolean isChecked() {
    return getValue();
  }

  public void setChecked(final boolean checked) {
    setValue(checked);
  }

  @Override
  public String getName() {
    return getInputElement().getName();
  }

  @Override
  public void setName(final String name) {
    getInputElement().setName(name);
  }

  public String getFormValue() {
    return getInputElement().getValue();
  }

  public void setFormValue(final String value) {
    getInputElement().setValue(value);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Boolean> handler) {
    if (!valueChangeHandlerInitialized) {
      ensureDomEventHandlers();
      valueChangeHandlerInitialized = true;
    }
    return addHandler(handler, ValueChangeEvent.<Boolean>getType());
  }

  /** Turns the browser click into the logical value-change event. */
  protected void ensureDomEventHandlers() {
    addClickHandler(event -> ValueChangeEvent.fire(this, getValue()));
  }
}
