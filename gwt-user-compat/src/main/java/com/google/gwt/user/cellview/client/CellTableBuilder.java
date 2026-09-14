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
package com.google.gwt.user.cellview.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;

/** Builds the body rows of a table, for a view that wants to depart from one row per value. */
public interface CellTableBuilder<T> {

  /** Builds the markup for one row. */
  void buildRowImpl(T rowValue, int absRowIndex);

  /** The column a rendered cell belongs to, given the element it was rendered into. */
  Column<T, ?> getColumn(Context context, T rowValue, Element elem);

  Iterable<Column<T, ?>> getColumns();

  /** Where in the view a row is being built. */
  class Context {

    private final int index;
    private final int column;
    private final Object key;

    public Context(final int index, final int column, final Object key) {
      this.index = index;
      this.column = column;
      this.key = key;
    }

    public int getIndex() {
      return index;
    }

    public int getColumn() {
      return column;
    }

    public Object getKey() {
      return key;
    }
  }

  /** True when the element is inside a cell that consumed the event. */
  boolean isColumn(Element elem);

  /** The finished markup. */
  SafeHtml finish();

  void start(boolean isRebuildingAllRows);
}
