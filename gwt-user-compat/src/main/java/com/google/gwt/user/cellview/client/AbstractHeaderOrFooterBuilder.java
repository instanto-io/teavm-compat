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

/** Base builder for a table's header or footer row. */
public abstract class AbstractHeaderOrFooterBuilder<T>
    implements HeaderBuilder<T>, FooterBuilder<T> {

  private final AbstractCellTable<T> table;
  private final boolean isFooter;
  private boolean sortIconStartOfLine = true;

  public AbstractHeaderOrFooterBuilder(final AbstractCellTable<T> table, final boolean isFooter) {
    this.table = table;
    this.isFooter = isFooter;
  }

  protected AbstractCellTable<T> getTable() {
    return table;
  }

  public boolean isBuildingFooter() {
    return isFooter;
  }

  public boolean isSortIconStartOfLine() {
    return sortIconStartOfLine;
  }

  public void setSortIconStartOfLine(final boolean startOfLine) {
    this.sortIconStartOfLine = startOfLine;
  }

  @Override
  public SafeHtml buildHeader() {
    final SafeHtmlBuilder sb = new SafeHtmlBuilder();
    return buildHeaderOrFooterImpl(sb) ? sb.toSafeHtml() : null;
  }

  @Override
  public SafeHtml buildFooter() {
    final SafeHtmlBuilder sb = new SafeHtmlBuilder();
    return buildHeaderOrFooterImpl(sb) ? sb.toSafeHtml() : null;
  }

  @Override
  public Header<?> getHeader(final Element elem) {
    final int index = columnIndexOf(elem);
    if (index < 0) {
      return null;
    }
    return isFooter ? table.getFooter(index) : table.getHeader(index);
  }

  @Override
  public Column<T, ?> getColumn(final Element elem) {
    final int index = columnIndexOf(elem);
    return index < 0 ? null : table.getColumn(index);
  }

  @Override
  public boolean isHeader(final Element elem) {
    return !isFooter && elem != null && "th".equalsIgnoreCase(elem.getTagName());
  }

  @Override
  public boolean isFooter(final Element elem) {
    return isFooter && elem != null && "td".equalsIgnoreCase(elem.getTagName());
  }

  @Override
  public boolean isSortIcon(final Element elem) {
    return elem != null && elem.hasClassName("sort-icon");
  }

  /** Renders one header cell. */
  protected <H> void renderHeader(
      final SafeHtmlBuilder sb, final Cell.Context context, final Header<H> header) {
    header.getCell().render(context, header.getValue(), sb);
  }

  /** Writes the row; returns false when there is nothing to show. */
  protected abstract boolean buildHeaderOrFooterImpl(SafeHtmlBuilder sb);

  private int columnIndexOf(final Element elem) {
    if (elem == null) {
      return -1;
    }
    Element cell = elem;
    while (cell != null
        && !"th".equalsIgnoreCase(cell.getTagName())
        && !"td".equalsIgnoreCase(cell.getTagName())) {
      cell = cell.getParentElement();
    }
    if (cell == null) {
      return -1;
    }
    final Element row = cell.getParentElement();
    if (row == null) {
      return -1;
    }
    int index = 0;
    Element current = row.getFirstChildElement();
    while (current != null) {
      if (current.unwrap() == cell.unwrap()) {
        return index;
      }
      index++;
      current = current.getNextSiblingElement();
    }
    return -1;
  }
}
