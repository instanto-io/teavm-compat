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
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.safehtml.shared.SafeHtml;

/** A mutually exclusive selection control with an associated label. */
public class RadioButton extends CheckBox {

  public RadioButton(final String name) {
    super(Document.get().createRadioInputElement(name));
    setStyleName("gwt-RadioButton");
  }

  public RadioButton(final String name, final String label) {
    this(name);
    setText(label);
  }

  public static final DirectionEstimator DEFAULT_DIRECTION_ESTIMATOR =
      DirectionalTextHelper.DEFAULT_DIRECTION_ESTIMATOR;

  public RadioButton(final String name, final SafeHtml label) {
    this(name, label.asString(), true);
  }

  public RadioButton(final String name, final SafeHtml label, final Direction direction) {
    this(name);
    setHTML(label, direction);
  }

  public RadioButton(final String name, final SafeHtml label, final DirectionEstimator estimator) {
    this(name);
    setDirectionEstimator(estimator);
    setHTML(label.asString());
  }

  public RadioButton(final String name, final String label, final Direction direction) {
    this(name);
    setText(label, direction);
  }

  public RadioButton(final String name, final String label, final DirectionEstimator estimator) {
    this(name);
    setDirectionEstimator(estimator);
    setText(label);
  }

  public RadioButton(final String name, final String label, final boolean asHTML) {
    this(name);
    if (asHTML) {
      setHTML(label);
    } else {
      setText(label);
    }
  }

  @Override
  public void setValue(final Boolean value, final boolean fireEvents) {
    super.setValue(value, fireEvents);
  }
}
