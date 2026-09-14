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
package com.google.gwt.cell.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/** A checkbox reflecting a boolean value. */
public class CheckboxCell extends AbstractEditableCell<Boolean, Boolean> {

  private final boolean dependsOnSelection;
  private final boolean handlesSelection;

  public CheckboxCell() {
    this(false, false);
  }

  public CheckboxCell(final boolean dependsOnSelection, final boolean handlesSelection) {
    super("change", "keydown");
    this.dependsOnSelection = dependsOnSelection;
    this.handlesSelection = handlesSelection;
  }

  @Override
  public boolean dependsOnSelection() {
    return dependsOnSelection;
  }

  @Override
  public boolean handlesSelection() {
    return handlesSelection;
  }

  @Override
  public boolean isEditing(final Context context, final Element parent, final Boolean value) {
    return false;
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final Boolean value,
      final NativeEvent event,
      final ValueUpdater<Boolean> valueUpdater) {
    final String type = event.getType();
    if (!"change".equals(type)) {
      return;
    }
    final InputElement input = InputElement.as(parent.getFirstChildElement());
    if (input == null) {
      return;
    }
    final Boolean isChecked = input.isChecked();
    if (handlesSelection()) {
      setViewData(context.getKey(), isChecked);
    } else {
      clearViewData(context.getKey());
    }
    if (valueUpdater != null) {
      valueUpdater.update(isChecked);
    }
  }

  @Override
  public void render(final Context context, final Boolean value, final SafeHtmlBuilder sb) {
    final Boolean viewData = getViewData(context.getKey());
    final boolean checked = viewData == null ? Boolean.TRUE.equals(value) : viewData;
    if (viewData != null && viewData.equals(value)) {
      clearViewData(context.getKey());
    }
    sb.appendHtmlConstant(
        "<input type=\"checkbox\" tabindex=\"-1\"" + (checked ? " checked" : "") + "/>");
  }
}
