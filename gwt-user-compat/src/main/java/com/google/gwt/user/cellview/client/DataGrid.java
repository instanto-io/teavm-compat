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

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A table with a fixed header and a scrolling body.
 *
 * <p>Shares its rendering with {@link CellTable}; the fixed-header layout GWT builds from nested
 * scroll panels is left to CSS here.
 */
public class DataGrid<T> extends AbstractCellTable<T> {

  /** GWT's default page size for a DataGrid. */
  public static final int DEFAULT_PAGESIZE = 50;

  /** Style names a DataGrid applies to its parts. */
  public interface Style extends com.google.gwt.resources.client.CssResource {
    String dataGridCell();

    String dataGridEvenRow();

    String dataGridEvenRowCell();

    String dataGridFirstColumn();

    String dataGridFirstColumnFooter();

    String dataGridFirstColumnHeader();

    String dataGridFooter();

    String dataGridHeader();

    String dataGridHoveredRow();

    String dataGridHoveredRowCell();

    String dataGridKeyboardSelectedCell();

    String dataGridKeyboardSelectedRow();

    String dataGridKeyboardSelectedRowCell();

    String dataGridLastColumn();

    String dataGridLastColumnFooter();

    String dataGridLastColumnHeader();

    String dataGridOddRow();

    String dataGridOddRowCell();

    String dataGridSelectedRow();

    String dataGridSelectedRowCell();

    String dataGridSortableHeader();

    String dataGridSortedHeaderAscending();

    String dataGridSortedHeaderDescending();

    String dataGridWidget();
  }

  /** Images and styles a DataGrid uses. */
  public interface Resources extends com.google.gwt.resources.client.ClientBundle {
    com.google.gwt.resources.client.ImageResource dataGridLoading();

    com.google.gwt.resources.client.ImageResource dataGridSortAscending();

    com.google.gwt.resources.client.ImageResource dataGridSortDescending();

    Style dataGridStyle();
  }

  private Widget loadingIndicator;

  public DataGrid() {
    this(DEFAULT_PAGESIZE);
  }

  public DataGrid(final int pageSize) {
    this(pageSize, null, null, null);
  }

  public DataGrid(final int pageSize, final Resources resources) {
    this(pageSize, resources, null, null);
  }

  public DataGrid(final int pageSize, final ProvidesKey<T> keyProvider) {
    this(pageSize, null, keyProvider, null);
  }

  public DataGrid(final int pageSize, final Resources resources, final ProvidesKey<T> keyProvider) {
    this(pageSize, resources, keyProvider, null);
  }

  public DataGrid(
      final int pageSize,
      final Resources resources,
      final ProvidesKey<T> keyProvider,
      final Widget loadingIndicator) {
    super(Document.get().createTableElement(), pageSize, keyProvider);
    this.loadingIndicator = loadingIndicator;
    setStyleName("dataGrid");
  }

  public Widget getLoadingIndicator() {
    return loadingIndicator;
  }

  public void setLoadingIndicator(final Widget loadingIndicator) {
    this.loadingIndicator = loadingIndicator;
  }

  public void setColumnWidth(final Column<T, ?> column, final String width) {}

  public void setColumnWidth(final Column<T, ?> column, final double width, final Object unit) {}

  public void setMinimumTableWidth(final double width, final Object unit) {}

  public void setTableWidth(final double width, final Object unit) {}

  public void setEmptyTableWidget(final Widget widget) {}
}
