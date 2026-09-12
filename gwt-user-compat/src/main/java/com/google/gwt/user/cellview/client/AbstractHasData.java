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

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the row data, visible range and selection for a data-bound view, and re-renders when any of
 * them changes.
 *
 * <p>Rendering is whole-view rather than incremental. GWT's implementation patches individual rows
 * to stay fast over very long lists; this re-renders the visible page, which is simpler and
 * adequate for the page sizes a table actually shows.
 */
public abstract class AbstractHasData<T> extends Widget implements HasData<T> {

  private final List<T> rowData = new ArrayList<>();
  private ProvidesKey<T> keyProvider;
  private SelectionModel<? super T> selectionModel;
  private HandlerRegistration selectionRegistration;
  private Range visibleRange;
  private int rowCount;
  private boolean rowCountExact;
  private boolean pendingRefresh;

  public AbstractHasData(final int pageSize, final ProvidesKey<T> keyProvider) {
    this.keyProvider = keyProvider;
    this.visibleRange = new Range(0, pageSize);
  }

  public ProvidesKey<T> getKeyProvider() {
    return keyProvider;
  }

  @Override
  public T getValueKey(final T value) {
    return value;
  }

  /** The key for a row, from the key provider when one is set. */
  public Object getRowKey(final T value) {
    return keyProvider == null ? value : keyProvider.getKey(value);
  }

  @Override
  public HandlerRegistration addRangeChangeHandler(final RangeChangeEvent.Handler handler) {
    return addHandler(handler, RangeChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addRowCountChangeHandler(final RowCountChangeEvent.Handler handler) {
    return addHandler(handler, RowCountChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addCellPreviewHandler(final CellPreviewEvent.Handler<T> handler) {
    return addHandler(handler, CellPreviewEvent.<T>getType());
  }

  @Override
  public int getRowCount() {
    return rowCount;
  }

  @Override
  public boolean isRowCountExact() {
    return rowCountExact;
  }

  @Override
  public void setRowCount(final int count) {
    setRowCount(count, true);
  }

  @Override
  public void setRowCount(final int count, final boolean isExact) {
    if (count == rowCount && isExact == rowCountExact) {
      return;
    }
    rowCount = count;
    rowCountExact = isExact;
    RowCountChangeEvent.fire(this, count, isExact);
    refresh();
  }

  @Override
  public Range getVisibleRange() {
    return visibleRange;
  }

  @Override
  public void setVisibleRange(final int start, final int length) {
    setVisibleRange(new Range(start, length));
  }

  @Override
  public void setVisibleRange(final Range range) {
    if (range == null || range.equals(visibleRange)) {
      return;
    }
    visibleRange = range;
    RangeChangeEvent.fire(this, range);
    refresh();
  }

  public int getPageSize() {
    return visibleRange.getLength();
  }

  public void setPageSize(final int pageSize) {
    setVisibleRange(visibleRange.getStart(), pageSize);
  }

  public int getPageStart() {
    return visibleRange.getStart();
  }

  public void setPageStart(final int pageStart) {
    setVisibleRange(pageStart, visibleRange.getLength());
  }

  @Override
  public void setRowData(final int start, final List<? extends T> values) {
    final int offset = start - visibleRange.getStart();
    while (rowData.size() < offset + values.size()) {
      rowData.add(null);
    }
    for (int i = 0; i < values.size(); i++) {
      final int index = offset + i;
      if (index >= 0) {
        rowData.set(index, values.get(i));
      }
    }
    if (rowCount < start + values.size()) {
      rowCount = start + values.size();
    }
    refresh();
  }

  /** Replaces the whole visible page. */
  public void setRowData(final List<? extends T> values) {
    rowData.clear();
    rowData.addAll(values);
    setRowCount(values.size(), true);
    refresh();
  }

  @Override
  public T getVisibleItem(final int indexOnPage) {
    return rowData.get(indexOnPage);
  }

  @Override
  public int getVisibleItemCount() {
    return rowData.size();
  }

  @Override
  public Iterable<T> getVisibleItems() {
    return new ArrayList<>(rowData);
  }

  /** The rows currently held, in page order. */
  protected List<T> getRowData() {
    return rowData;
  }

  @Override
  public SelectionModel<? super T> getSelectionModel() {
    return selectionModel;
  }

  @Override
  public void setSelectionModel(final SelectionModel<? super T> selectionModel) {
    if (selectionRegistration != null) {
      selectionRegistration.removeHandler();
      selectionRegistration = null;
    }
    this.selectionModel = selectionModel;
    if (selectionModel != null) {
      selectionRegistration = selectionModel.addSelectionChangeHandler(event -> refresh());
    }
    refresh();
  }

  /** True when the row is selected under the current selection model. */
  protected boolean isRowSelected(final T value) {
    return selectionModel != null && value != null && selectionModel.isSelected(value);
  }

  /** Marks the view dirty; subclasses render on the next call to {@link #render()}. */
  public void redraw() {
    refresh();
  }

  protected void refresh() {
    if (pendingRefresh) {
      return;
    }
    pendingRefresh = true;
    try {
      render();
    } finally {
      pendingRefresh = false;
    }
  }

  /** Renders the current page into the widget's element. */
  protected abstract void render();

  @Override
  protected void onAttach() {
    super.onAttach();
    render();
  }
}
