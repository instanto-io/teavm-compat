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

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Base row builder: accumulates markup for the rows a table asks it to build.
 *
 * <p>Subclasses implement {@link #buildRowImpl} and append through {@link #getBuilder()}.
 */
public abstract class AbstractCellTableBuilder<T> implements CellTableBuilder<T> {

  private final AbstractCellTable<T> table;
  private SafeHtmlBuilder builder = new SafeHtmlBuilder();

  public AbstractCellTableBuilder(final AbstractCellTable<T> table) {
    this.table = table;
  }

  protected AbstractCellTable<T> getTable() {
    return table;
  }

  protected SafeHtmlBuilder getBuilder() {
    return builder;
  }

  @Override
  public void start(final boolean isRebuildingAllRows) {
    builder = new SafeHtmlBuilder();
  }

  @Override
  public SafeHtml finish() {
    return builder.toSafeHtml();
  }

  @Override
  public Iterable<Column<T, ?>> getColumns() {
    final List<Column<T, ?>> columns = new ArrayList<>();
    for (int i = 0; i < table.getColumnCount(); i++) {
      columns.add(table.getColumn(i));
    }
    return columns;
  }

  @Override
  public Column<T, ?> getColumn(final Context context, final T rowValue, final Element elem) {
    final int index = context.getColumn();
    return index < 0 || index >= table.getColumnCount() ? null : table.getColumn(index);
  }

  @Override
  public boolean isColumn(final Element elem) {
    return elem != null && "td".equalsIgnoreCase(elem.getTagName());
  }

  /** Renders one cell into the builder. */
  protected <C> void renderCell(
      final SafeHtmlBuilder sb,
      final Cell.Context context,
      final Column<T, C> column,
      final T rowValue) {
    column.getCell().render(context, column.getValue(rowValue), sb);
  }

  @Override
  public abstract void buildRowImpl(T rowValue, int absRowIndex);
}
