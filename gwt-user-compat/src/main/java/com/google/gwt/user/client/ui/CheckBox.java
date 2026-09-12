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
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.DirectionEstimator;

/**
 * A checkbox with an associated label, rendered as an input plus a label element inside a span, as
 * GWT does.
 */
public class CheckBox extends ButtonBase
    implements HasName, HasValue<Boolean>, HasWordWrap, HasDirectionalSafeHtml {

  private final InputElement inputElem;
  private final LabelElement labelElem;
  private final DirectionalTextHelper directionalTextHelper;
  private boolean valueChangeHandlerInitialized;

  public CheckBox() {
    this(Document.get().createCheckInputElement());
  }

  public CheckBox(final String label) {
    this();
    setText(label);
  }

  protected CheckBox(final Element inputElement) {
    super(Document.get().createSpanElement());
    inputElem = InputElement.as(inputElement);
    labelElem = LabelElement.as(Document.get().createLabelElement());

    final String uid = Document.get().createUniqueId();
    inputElem.setId(uid);
    labelElem.setAttribute("for", uid);

    getElement().appendChild(inputElem);
    getElement().appendChild(labelElem);

    directionalTextHelper = new DirectionalTextHelper(labelElem, true);
  }

  protected InputElement getInputElement() {
    return inputElem;
  }

  @Override
  public String getText() {
    return directionalTextHelper.getTextOrHtml(false);
  }

  @Override
  public void setText(final String text) {
    directionalTextHelper.setTextOrHtml(text, false);
  }

  public void setText(final String text, final Direction direction) {
    directionalTextHelper.setTextOrHtml(text, direction, false);
  }

  @Override
  public String getHTML() {
    return directionalTextHelper.getTextOrHtml(true);
  }

  @Override
  public void setHTML(final String html) {
    directionalTextHelper.setTextOrHtml(html, true);
  }

  @Override
  public void setHTML(
      final com.google.gwt.safehtml.shared.SafeHtml html, final Direction direction) {
    directionalTextHelper.setTextOrHtml(html == null ? "" : html.asString(), direction, true);
  }

  @Override
  public Direction getTextDirection() {
    return directionalTextHelper.getTextDirection();
  }

  public DirectionEstimator getDirectionEstimator() {
    return directionalTextHelper.getDirectionEstimator();
  }

  public void setDirectionEstimator(final boolean enabled) {
    directionalTextHelper.setDirectionEstimator(enabled);
  }

  public void setDirectionEstimator(final DirectionEstimator estimator) {
    directionalTextHelper.setDirectionEstimator(estimator);
  }

  @Override
  public Boolean getValue() {
    return inputElem.isChecked();
  }

  @Override
  public void setValue(final Boolean value) {
    setValue(value, false);
  }

  @Override
  public void setValue(final Boolean value, final boolean fireEvents) {
    final boolean effective = value != null && value;
    final Boolean old = fireEvents ? getValue() : null;
    inputElem.setChecked(effective);
    inputElem.setDefaultChecked(effective);
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, old, effective);
    }
  }

  @Override
  public String getName() {
    return inputElem.getName();
  }

  @Override
  public void setName(final String name) {
    inputElem.setName(name);
  }

  public String getFormValue() {
    return inputElem.getValue();
  }

  public void setFormValue(final String value) {
    inputElem.setValue(value);
  }

  @Override
  public boolean isEnabled() {
    return !inputElem.isDisabled();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    inputElem.setDisabled(!enabled);
  }

  @Override
  public boolean getWordWrap() {
    return !"nowrap".equals(getElement().getStyle().getProperty("white-space"));
  }

  @Override
  public void setWordWrap(final boolean wrap) {
    getElement().getStyle().setProperty("white-space", wrap ? "normal" : "nowrap");
  }

  @Override
  public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Boolean> handler) {
    if (!valueChangeHandlerInitialized) {
      ensureDomEventHandlers();
      valueChangeHandlerInitialized = true;
    }
    return addHandler(handler, ValueChangeEvent.<Boolean>getType());
  }

  protected void ensureDomEventHandlers() {
    addClickHandler(event -> ValueChangeEvent.fire(this, getValue()));
  }
}
