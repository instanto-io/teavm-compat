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
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/** One column of a cell table: a cell plus the accessor that feeds it. */
public abstract class Column<T, C> implements HasCell<T, C> {

  private final Cell<C> cell;
  private FieldUpdater<T, C> fieldUpdater;
  private boolean sortable;
  private boolean defaultSortAscending = true;
  private String cellStyleNames;
  private String horizontalAlignment;
  private String dataStoreName;

  public Column(final Cell<C> cell) {
    this.cell = cell;
  }

  @Override
  public Cell<C> getCell() {
    return cell;
  }

  @Override
  public FieldUpdater<T, C> getFieldUpdater() {
    return fieldUpdater;
  }

  public void setFieldUpdater(final FieldUpdater<T, C> fieldUpdater) {
    this.fieldUpdater = fieldUpdater;
  }

  @Override
  public abstract C getValue(T object);

  public boolean isSortable() {
    return sortable;
  }

  public void setSortable(final boolean sortable) {
    this.sortable = sortable;
  }

  public boolean isDefaultSortAscending() {
    return defaultSortAscending;
  }

  public void setDefaultSortAscending(final boolean ascending) {
    this.defaultSortAscending = ascending;
  }

  public String getCellStyleNames(final Cell.Context context, final T object) {
    return cellStyleNames;
  }

  public void setCellStyleNames(final String cellStyleNames) {
    this.cellStyleNames = cellStyleNames;
  }

  public String getHorizontalAlignment() {
    return horizontalAlignment;
  }

  public void setHorizontalAlignment(final String alignment) {
    this.horizontalAlignment = alignment;
  }

  public String getDataStoreName() {
    return dataStoreName;
  }

  public void setDataStoreName(final String dataStoreName) {
    this.dataStoreName = dataStoreName;
  }

  public void render(final Cell.Context context, final T object, final SafeHtmlBuilder sb) {
    cell.render(context, getValue(object), sb);
  }

  public void onBrowserEvent(
      final Cell.Context context, final Element elem, final T object, final NativeEvent event) {
    cell.onBrowserEvent(
        context,
        elem,
        getValue(object),
        event,
        value -> {
          if (fieldUpdater != null) {
            fieldUpdater.update(context.getIndex(), object, value);
          }
        });
  }
}
