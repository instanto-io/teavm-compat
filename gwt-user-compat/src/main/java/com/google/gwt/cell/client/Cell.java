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
import java.util.Set;

/** Renders a value into a cell of a view, and handles the browser events that value consumes. */
public interface Cell<C> {

  /** Where in the view a cell is being rendered. */
  class Context {

    private final int index;
    private final int column;
    private final Object key;
    private final int subIndex;

    public Context(final int index, final int column, final Object key) {
      this(index, column, key, 0);
    }

    public Context(final int index, final int column, final Object key, final int subIndex) {
      this.index = index;
      this.column = column;
      this.key = key;
      this.subIndex = subIndex;
    }

    /** The absolute row index, counting from the start of the data set. */
    public int getIndex() {
      return index;
    }

    public int getColumn() {
      return column;
    }

    public Object getKey() {
      return key;
    }

    public int getSubIndex() {
      return subIndex;
    }
  }

  /** True when the rendering depends on whether the row is selected. */
  boolean dependsOnSelection();

  /** The browser event types this cell wants delivered. */
  Set<String> getConsumedEvents();

  /** True when this cell handles selection itself. */
  boolean handlesSelection();

  boolean isEditing(Context context, Element parent, C value);

  void onBrowserEvent(
      Context context, Element parent, C value, NativeEvent event, ValueUpdater<C> valueUpdater);

  void render(Context context, C value, SafeHtmlBuilder sb);

  /** Restores focus after the cell is re-rendered; true when focus was taken. */
  boolean resetFocus(Context context, Element parent, C value);

  void setValue(Context context, Element parent, C value);
}
