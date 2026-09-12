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
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Renders rows and columns into a table element and routes events back to the cells. */
public abstract class AbstractCellTable<T> extends AbstractHasData<T> {

  private final List<Column<T, ?>> columns = new ArrayList<>();
  private final List<Header<?>> headers = new ArrayList<>();
  private final List<Header<?>> footers = new ArrayList<>();
  private final Set<String> boundEvents = new HashSet<>();

  private final Element table;
  private final Element thead;
  private final Element tbody;
  private final Element tfoot;

  private String emptyTableText = "";
  private RowStyles<T> rowStyles;
  private final ColumnSortList sortList = new ColumnSortList(this::refresh);
  private boolean headerListenerBound;

  public AbstractCellTable(
      final Element element, final int pageSize, final ProvidesKey<T> keyProvider) {
    super(pageSize, keyProvider);
    table = element;
    thead = Document.get().createElement("thead");
    tbody = Document.get().createElement("tbody");
    tfoot = Document.get().createElement("tfoot");
    table.appendChild(thead);
    table.appendChild(tbody);
    table.appendChild(tfoot);
    setElement(table);
  }

  public Element getTableHeadElement() {
    return thead;
  }

  public Element getTableBodyElement() {
    return tbody;
  }

  public Element getTableFootElement() {
    return tfoot;
  }

  public Element getTableLoadingSection() {
    return tbody;
  }

  public void addColumn(final Column<T, ?> column) {
    addColumn(column, (Header<?>) null, null);
  }

  public void addColumn(final Column<T, ?> column, final String headerText) {
    addColumn(column, new TextHeader(headerText), null);
  }

  public void addColumn(final Column<T, ?> column, final Header<?> header) {
    addColumn(column, header, null);
  }

  public void addColumn(final Column<T, ?> column, final Header<?> header, final Header<?> footer) {
    columns.add(column);
    headers.add(header);
    footers.add(footer);
    bindEventsFor(column.getCell());
    refresh();
  }

  public void insertColumn(
      final int beforeIndex,
      final Column<T, ?> column,
      final Header<?> header,
      final Header<?> footer) {
    columns.add(beforeIndex, column);
    headers.add(beforeIndex, header);
    footers.add(beforeIndex, footer);
    bindEventsFor(column.getCell());
    refresh();
  }

  public void removeColumn(final Column<T, ?> column) {
    final int index = columns.indexOf(column);
    if (index >= 0) {
      removeColumn(index);
    }
  }

  public void removeColumn(final int index) {
    columns.remove(index);
    headers.remove(index);
    footers.remove(index);
    refresh();
  }

  public int getColumnCount() {
    return columns.size();
  }

  public Column<T, ?> getColumn(final int index) {
    return columns.get(index);
  }

  public int getColumnIndex(final Column<T, ?> column) {
    return columns.indexOf(column);
  }

  public Header<?> getHeader(final int index) {
    return headers.get(index);
  }

  public Header<?> getFooter(final int index) {
    return footers.get(index);
  }

  /** The columns this table is sorted by, most significant first. */
  public ColumnSortList getColumnSortList() {
    return sortList;
  }

  public com.google.gwt.event.shared.HandlerRegistration addColumnSortHandler(
      final ColumnSortEvent.Handler handler) {
    return addHandler(handler, ColumnSortEvent.getType());
  }

  /** Style put on a header whose column can be sorted. */
  protected String getSortableHeaderStyle() {
    return "sortable";
  }

  /** Styles put on the header of the column currently sorted. */
  protected String getSortedAscendingStyle() {
    return "sorted-ascending";
  }

  protected String getSortedDescendingStyle() {
    return "sorted-descending";
  }

  /** GWT takes a widget here; the String overload is kept for convenience. */
  public void setEmptyTableWidget(final com.google.gwt.user.client.ui.Widget widget) {
    setEmptyTableWidget(widget == null ? "" : widget.getElement().getInnerHTML());
  }

  public void setEmptyTableWidget(final String text) {
    emptyTableText = text == null ? "" : text;
    refresh();
  }

  public RowStyles<T> getRowStyles() {
    return rowStyles;
  }

  public void setRowStyles(final RowStyles<T> rowStyles) {
    this.rowStyles = rowStyles;
    refresh();
  }

  /** Style applied to the row of a selected value; subclasses may override. */
  protected String getSelectedRowStyle() {
    return "selected";
  }

  @Override
  protected void render() {
    ensureHeaderListener();
    renderSection(thead, headers, true);
    renderSection(tfoot, footers, false);
    renderBody();
  }

  private void renderSection(
      final Element section, final List<Header<?>> cells, final boolean isHeader) {
    section.setInnerHTML("");
    boolean any = false;
    for (final Header<?> header : cells) {
      if (header != null) {
        any = true;
        break;
      }
    }
    if (!any) {
      return;
    }
    final SafeHtmlBuilder sb = new SafeHtmlBuilder();
    sb.appendHtmlConstant("<tr>");
    for (int i = 0; i < cells.size(); i++) {
      final Header<?> header = cells.get(i);
      final String tag = isHeader ? "th" : "td";
      final StringBuilder classes = new StringBuilder();
      final String styles = header == null ? null : header.getHeaderStyleNames();
      if (styles != null && !styles.isEmpty()) {
        classes.append(styles);
      }
      if (isHeader && i < columns.size() && columns.get(i).isSortable()) {
        appendClass(classes, getSortableHeaderStyle());
        final ColumnSortList.ColumnSortInfo first = sortList.size() == 0 ? null : sortList.get(0);
        if (first != null && first.getColumn() == columns.get(i)) {
          appendClass(
              classes,
              first.isAscending() ? getSortedAscendingStyle() : getSortedDescendingStyle());
        }
      }
      sb.appendHtmlConstant(
          "<" + tag + (classes.length() == 0 ? "" : " class=\"" + classes + "\"") + ">");
      if (header != null) {
        header.render(new Cell.Context(0, i, null), sb);
      }
      sb.appendHtmlConstant("</" + tag + ">");
    }
    sb.appendHtmlConstant("</tr>");
    section.setInnerSafeHtml(sb.toSafeHtml());
  }

  private static void appendClass(final StringBuilder classes, final String name) {
    if (name == null || name.isEmpty()) {
      return;
    }
    if (classes.length() > 0) {
      classes.append(' ');
    }
    classes.append(name);
  }

  private void renderBody() {
    final List<T> rows = getRowData();
    final SafeHtmlBuilder sb = new SafeHtmlBuilder();

    if (rows.isEmpty()) {
      sb.appendHtmlConstant("<tr><td colspan=\"" + Math.max(1, columns.size()) + "\">");
      sb.appendEscaped(emptyTableText);
      sb.appendHtmlConstant("</td></tr>");
      tbody.setInnerSafeHtml(sb.toSafeHtml());
      return;
    }

    final int pageStart = getPageStart();
    for (int r = 0; r < rows.size(); r++) {
      final T value = rows.get(r);
      final int absoluteIndex = pageStart + r;
      final StringBuilder rowClass = new StringBuilder();
      if (isRowSelected(value)) {
        rowClass.append(getSelectedRowStyle());
      }
      if (rowStyles != null) {
        final String extra = rowStyles.getStyleNames(value, absoluteIndex);
        if (extra != null && !extra.isEmpty()) {
          if (rowClass.length() > 0) {
            rowClass.append(' ');
          }
          rowClass.append(extra);
        }
      }
      sb.appendHtmlConstant(
          "<tr" + (rowClass.length() == 0 ? "" : " class=\"" + rowClass + "\"") + ">");
      for (int c = 0; c < columns.size(); c++) {
        final Column<T, ?> column = columns.get(c);
        final Cell.Context context = new Cell.Context(absoluteIndex, c, getRowKey(value));
        final String cellStyles = column.getCellStyleNames(context, value);
        final String align = column.getHorizontalAlignment();
        sb.appendHtmlConstant(
            "<td"
                + (cellStyles == null ? "" : " class=\"" + cellStyles + "\"")
                + (align == null ? "" : " style=\"text-align:" + align + "\"")
                + ">");
        column.render(context, value, sb);
        sb.appendHtmlConstant("</td>");
      }
      sb.appendHtmlConstant("</tr>");
    }
    tbody.setInnerSafeHtml(sb.toSafeHtml());
  }

  /** Binds the header click that drives sorting, once. */
  private void ensureHeaderListener() {
    if (headerListenerBound) {
      return;
    }
    headerListenerBound = true;
    thead.unwrap().addEventListener("click", this::onHeaderClick);
  }

  private void onHeaderClick(final org.teavm.jso.dom.events.Event nativeEvent) {
    final NativeEvent event = new NativeEvent(nativeEvent);
    final Element target = Element.as(event.getEventTarget());
    if (target == null) {
      return;
    }
    final Element cellElement = closest(target, "th");
    if (cellElement == null || !thead.isOrHasChild(cellElement)) {
      return;
    }
    final Element rowElement = closest(cellElement, "tr");
    if (rowElement == null) {
      return;
    }
    final int columnIndex = indexOfChild(rowElement, cellElement);
    if (columnIndex < 0 || columnIndex >= columns.size()) {
      return;
    }

    final Header<?> header = headers.get(columnIndex);
    if (header != null) {
      header.onBrowserEvent(new Cell.Context(0, columnIndex, null), cellElement, event);
    }

    final Column<T, ?> column = columns.get(columnIndex);
    if (column.isSortable()) {
      sortList.push(column);
      ColumnSortEvent.fire(this, sortList);
    }
  }

  /** Attaches a listener for each event type a cell consumes, once per type. */
  private void bindEventsFor(final Cell<?> cell) {
    final Set<String> events = cell == null ? null : cell.getConsumedEvents();
    if (events == null) {
      return;
    }
    for (final String eventName : events) {
      if (boundEvents.add(eventName)) {
        table.unwrap().addEventListener(eventName, this::dispatch);
      }
    }
  }

  /** Routes a browser event to the cell whose {@code td} it landed in. */
  private void dispatch(final org.teavm.jso.dom.events.Event nativeEvent) {
    final NativeEvent event = new NativeEvent(nativeEvent);
    final Element target = Element.as(event.getEventTarget());
    if (target == null) {
      return;
    }
    final Element cellElement = closest(target, "td");
    if (cellElement == null || !tbody.isOrHasChild(cellElement)) {
      return;
    }
    final Element rowElement = closest(cellElement, "tr");
    if (rowElement == null) {
      return;
    }
    final int columnIndex = indexOfChild(rowElement, cellElement);
    final int rowIndex = indexOfChild(tbody, rowElement);
    if (columnIndex < 0
        || rowIndex < 0
        || rowIndex >= getRowData().size()
        || columnIndex >= columns.size()) {
      return;
    }

    final T value = getRowData().get(rowIndex);
    final int absoluteIndex = getPageStart() + rowIndex;
    final Column<T, ?> column = columns.get(columnIndex);
    final Cell.Context context = new Cell.Context(absoluteIndex, columnIndex, getRowKey(value));

    final CellPreviewEvent<T> preview =
        CellPreviewEvent.fire(
            this,
            event,
            this,
            absoluteIndex,
            columnIndex,
            value,
            column.getCell().isEditing(context, cellElement, null),
            column.getCell().handlesSelection());
    if (preview.isCanceled()) {
      return;
    }

    column.onBrowserEvent(context, cellElement, value, event);

    final SelectionModel<? super T> selectionModel = getSelectionModel();
    if (selectionModel != null
        && "click".equals(event.getType())
        && !column.getCell().handlesSelection()) {
      selectionModel.setSelected(value, !selectionModel.isSelected(value));
    }
  }

  private static Element closest(final Element start, final String tagName) {
    Element current = start;
    while (current != null) {
      if (tagName.equalsIgnoreCase(current.getTagName())) {
        return current;
      }
      current = current.getParentElement();
    }
    return null;
  }

  private static int indexOfChild(final Element parent, final Element child) {
    int index = 0;
    Element current = parent.getFirstChildElement();
    while (current != null) {
      if (current.unwrap() == child.unwrap()) {
        return index;
      }
      index++;
      current = current.getNextSiblingElement();
    }
    return -1;
  }
}
