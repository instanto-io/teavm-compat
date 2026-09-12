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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;

/**
 * Page-navigation state for a paged view.
 *
 * <p>The cellview data-presentation stack is not part of the TeaVM port, so this carries page state
 * and exposes the navigation API that {@code Pagination} calls, without the GWT display bindings.
 */
public class SimplePager extends Composite {

  /** Where the pager places its controls relative to the table. */
  public enum TextLocation {
    CENTER,
    LEFT,
    RIGHT
  }

  private int pageSize = 10;
  private int pageStart;
  private int rowCount;
  private HasRows display;

  public SimplePager() {
    initWidget(new FlowPanel());
  }

  /**
   * Binds the pager to the view it pages.
   *
   * <p>GWT's AbstractPager holds the display and keeps the two in step in both directions: page
   * state is pushed to the display as a visible range, and the display's own range and row-count
   * changes are read back. Without this the pager counts pages that nothing turns.
   */
  public void setDisplay(final HasRows display) {
    this.display = display;
    if (display == null) {
      return;
    }
    display.addRangeChangeHandler(
        new RangeChangeEvent.Handler() {
          @Override
          public void onRangeChange(final RangeChangeEvent event) {
            final Range range = event.getNewRange();
            pageStart = range.getStart();
            pageSize = range.getLength();
          }
        });
    display.addRowCountChangeHandler(
        new RowCountChangeEvent.Handler() {
          @Override
          public void onRowCountChange(final RowCountChangeEvent event) {
            rowCount = event.getNewRowCount();
          }
        });
    final Range range = display.getVisibleRange();
    if (range != null) {
      pageStart = range.getStart();
      pageSize = range.getLength();
    }
    rowCount = display.getRowCount();
  }

  /** The view this pager pages, or null. */
  public HasRows getDisplay() {
    return display;
  }

  public int getPage() {
    return pageSize == 0 ? 0 : pageStart / pageSize;
  }

  public int getPageCount() {
    return pageSize == 0 ? 0 : (rowCount + pageSize - 1) / pageSize;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
    pushRange();
  }

  public int getPageStart() {
    return pageStart;
  }

  public void setPageStart(final int pageStart) {
    this.pageStart = Math.max(0, pageStart);
    pushRange();
  }

  public void setRowCount(final int rowCount) {
    this.rowCount = Math.max(0, rowCount);
    if (display != null) {
      display.setRowCount(this.rowCount);
    }
  }

  private void pushRange() {
    if (display != null) {
      display.setVisibleRange(pageStart, pageSize);
    }
  }

  public void setPage(final int index) {
    setPageStart(index * pageSize);
  }

  public boolean hasNextPage() {
    return pageStart + pageSize < rowCount;
  }

  public boolean hasPreviousPage() {
    return pageStart > 0;
  }

  public void nextPage() {
    if (hasNextPage()) {
      setPageStart(pageStart + pageSize);
    }
  }

  public void previousPage() {
    if (hasPreviousPage()) {
      setPageStart(pageStart - pageSize);
    }
  }

  public void firstPage() {
    setPageStart(0);
  }

  public void lastPage() {
    setPage(Math.max(0, getPageCount() - 1));
  }
}
