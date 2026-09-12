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
 * A table of rows and columns backed by a data provider.
 *
 * <p>Renders the visible page into a real {@code table}. GWT's implementation also does keyboard
 * navigation, column-sort affordances and incremental row patching; those are not reproduced here,
 * and the hooks that expose them are present but inert so that calling code compiles and behaves
 * predictably.
 */
public class CellTable<T> extends AbstractCellTable<T> {

  /** GWT's default page size. */
  public static final int DEFAULT_PAGESIZE = 15;

  /** Style names a table applies to its parts. */
  public interface Style extends com.google.gwt.resources.client.CssResource {
    String cellTableCell();

    String cellTableEvenRow();

    String cellTableEvenRowCell();

    String cellTableFirstColumn();

    String cellTableFirstColumnFooter();

    String cellTableFirstColumnHeader();

    String cellTableFooter();

    String cellTableHeader();

    String cellTableHoveredRow();

    String cellTableHoveredRowCell();

    String cellTableKeyboardSelectedCell();

    String cellTableKeyboardSelectedRow();

    String cellTableKeyboardSelectedRowCell();

    String cellTableLastColumn();

    String cellTableLastColumnFooter();

    String cellTableLastColumnHeader();

    String cellTableLoading();

    String cellTableOddRow();

    String cellTableOddRowCell();

    String cellTableSelectedRow();

    String cellTableSelectedRowCell();

    String cellTableSortableHeader();

    String cellTableSortedHeaderAscending();

    String cellTableSortedHeaderDescending();

    String cellTableWidget();
  }

  /** Images and styles a table uses. */
  public interface Resources extends com.google.gwt.resources.client.ClientBundle {
    com.google.gwt.resources.client.ImageResource cellTableFooterBackground();

    com.google.gwt.resources.client.ImageResource cellTableHeaderBackground();

    com.google.gwt.resources.client.ImageResource cellTableLoading();

    com.google.gwt.resources.client.ImageResource cellTableSelectedBackground();

    com.google.gwt.resources.client.ImageResource cellTableSortAscending();

    com.google.gwt.resources.client.ImageResource cellTableSortDescending();

    Style cellTableStyle();
  }

  private Widget loadingIndicator;

  public CellTable() {
    this(DEFAULT_PAGESIZE);
  }

  public CellTable(final int pageSize) {
    this(pageSize, null, null, null);
  }

  public CellTable(final int pageSize, final Resources resources) {
    this(pageSize, resources, null, null);
  }

  public CellTable(final ProvidesKey<T> keyProvider) {
    this(DEFAULT_PAGESIZE, null, keyProvider, null);
  }

  public CellTable(final int pageSize, final ProvidesKey<T> keyProvider) {
    this(pageSize, null, keyProvider, null);
  }

  public CellTable(
      final int pageSize, final Resources resources, final ProvidesKey<T> keyProvider) {
    this(pageSize, resources, keyProvider, null);
  }

  public CellTable(
      final int pageSize,
      final Resources resources,
      final ProvidesKey<T> keyProvider,
      final Widget loadingIndicator) {
    super(Document.get().createTableElement(), pageSize, keyProvider);
    this.loadingIndicator = loadingIndicator;
    setStyleName("cellTable");
  }

  public CellTable(
      final int pageSize,
      final Resources resources,
      final ProvidesKey<T> keyProvider,
      final Widget loadingIndicator,
      final boolean enableColGroup,
      final boolean attachLoadingPanel) {
    this(pageSize, resources, keyProvider, loadingIndicator);
  }

  public Widget getLoadingIndicator() {
    return loadingIndicator;
  }

  public void setLoadingIndicator(final Widget loadingIndicator) {
    this.loadingIndicator = loadingIndicator;
  }

  /** GWT sizes columns through a colgroup; sizing here is left to CSS. */
  public void setColumnWidth(final Column<T, ?> column, final String width) {}

  public void setColumnWidth(final Column<T, ?> column, final double width, final Object unit) {}

  public void clearColumnWidth(final Column<T, ?> column) {}

  public void setMinimumTableWidth(final double width, final Object unit) {}

  public void setTableWidth(final double width, final Object unit) {}

  public void setSkipRowHoverCheck(final boolean skip) {}

  public void setSkipRowHoverStyleUpdate(final boolean skip) {}
}
