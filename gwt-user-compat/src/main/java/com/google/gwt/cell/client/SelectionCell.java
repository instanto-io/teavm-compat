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
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A dropdown offering a fixed set of options. */
public class SelectionCell extends AbstractInputCell<String, String> {

  private final List<String> options;
  private final Map<String, Integer> indexForOption = new HashMap<>();

  public SelectionCell(final List<String> options) {
    super("change");
    this.options = new ArrayList<>(options == null ? new ArrayList<String>() : options);
    for (int i = 0; i < this.options.size(); i++) {
      indexForOption.put(this.options.get(i), i);
    }
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final String value,
      final NativeEvent event,
      final ValueUpdater<String> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if (!"change".equals(event.getType())) {
      return;
    }
    final Element select = parent.getFirstChildElement();
    if (select == null) {
      return;
    }
    final int index = select.getPropertyInt("selectedIndex");
    if (index < 0 || index >= options.size()) {
      return;
    }
    final String newValue = options.get(index);
    setViewData(context.getKey(), newValue);
    finishEditing(parent, newValue, context.getKey(), valueUpdater);
  }

  @Override
  public void render(final Context context, final String value, final SafeHtmlBuilder sb) {
    final String viewData = getViewData(context.getKey());
    if (viewData != null && viewData.equals(value)) {
      clearViewData(context.getKey());
    }
    final String selected = viewData == null ? value : viewData;
    final int selectedIndex = selected == null ? -1 : indexForOption.getOrDefault(selected, -1);

    sb.appendHtmlConstant("<select tabindex=\"-1\">");
    for (int i = 0; i < options.size(); i++) {
      sb.appendHtmlConstant("<option" + (i == selectedIndex ? " selected" : "") + ">");
      sb.appendEscaped(options.get(i));
      sb.appendHtmlConstant("</option>");
    }
    sb.appendHtmlConstant("</select>");
  }

  protected void finishEditing(
      final Element parent,
      final String value,
      final Object key,
      final ValueUpdater<String> valueUpdater) {
    if (valueUpdater != null) {
      valueUpdater.update(value);
    }
  }
}
