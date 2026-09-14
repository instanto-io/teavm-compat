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
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/** A permanently editable text input. */
public class TextInputCell extends AbstractEditableCell<String, String> {

  public TextInputCell() {
    super("change", "keyup");
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final String value,
      final NativeEvent event,
      final ValueUpdater<String> valueUpdater) {
    final InputElement input = InputElement.as(parent.getFirstChildElement());
    if (input == null) {
      return;
    }
    final String current = input.getValue();
    setViewData(context.getKey(), current);
    if ("change".equals(event.getType()) && valueUpdater != null) {
      valueUpdater.update(current);
    }
  }

  @Override
  public void render(final Context context, final String value, final SafeHtmlBuilder sb) {
    final String viewData = getViewData(context.getKey());
    if (viewData != null && viewData.equals(value)) {
      clearViewData(context.getKey());
    }
    final String shown = viewData == null ? value : viewData;
    sb.appendHtmlConstant(
        "<input type=\"text\" tabindex=\"-1\" value=\""
            + SafeHtmlUtils.htmlEscape(shown == null ? "" : shown)
            + "\"/>");
  }
}
