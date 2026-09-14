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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

/** Text that reports clicks without looking like a button. */
public class ClickableTextCell extends AbstractSafeHtmlCell<String> {

  public ClickableTextCell() {
    this(SimpleSafeHtmlRenderer.getInstance());
  }

  public ClickableTextCell(final SafeHtmlRenderer<String> renderer) {
    super(renderer, "click", "keydown");
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final String value,
      final NativeEvent event,
      final ValueUpdater<String> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if ("click".equals(event.getType())) {
      onEnterKeyDown(context, parent, value, event, valueUpdater);
    }
  }

  @Override
  protected void render(final Context context, final SafeHtml value, final SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(value);
    }
  }

  @Override
  protected void onEnterKeyDown(
      final Context context,
      final Element parent,
      final String value,
      final NativeEvent event,
      final ValueUpdater<String> valueUpdater) {
    if (valueUpdater != null) {
      valueUpdater.update(value);
    }
  }
}
