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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Base for cells whose rendering is a focusable form control. */
public abstract class AbstractInputCell<C, V> extends AbstractEditableCell<C, V> {

  private static Set<String> withFocus(final String... events) {
    final Set<String> all = new HashSet<>(Arrays.asList("focus", "blur", "keydown"));
    if (events != null) {
      all.addAll(Arrays.asList(events));
    }
    return all;
  }

  public AbstractInputCell(final String... consumedEvents) {
    super(withFocus(consumedEvents));
  }

  public AbstractInputCell(final Set<String> consumedEvents) {
    super(consumedEvents);
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater) {
    final String type = event.getType();
    if ("focus".equals(type)) {
      onFocus(context, parent, value);
    } else if ("blur".equals(type)) {
      onBlur(context, parent, value);
    }
  }

  protected void onFocus(final Context context, final Element parent, final C value) {}

  protected void onBlur(final Context context, final Element parent, final C value) {}

  /** The input this cell rendered, which is its first element child. */
  protected Element getInputElement(final Element parent) {
    return parent.getFirstChildElement();
  }
}
