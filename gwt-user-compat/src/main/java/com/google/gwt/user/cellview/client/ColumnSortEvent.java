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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Fired when the user sorts a view by clicking a sortable header. */
public class ColumnSortEvent extends GwtEvent<ColumnSortEvent.Handler> {

  /** Receives sort requests. */
  public interface Handler extends EventHandler {
    void onColumnSort(ColumnSortEvent event);
  }

  /**
   * Handler for a view whose data is fetched remotely: it re-requests the current range and leaves
   * the ordering to the server.
   */
  public abstract static class AsyncHandler implements Handler {

    private final AbstractHasData<?> hasData;

    public AsyncHandler(final AbstractHasData<?> hasData) {
      this.hasData = hasData;
    }

    @Override
    public void onColumnSort(final ColumnSortEvent event) {
      hasData.redraw();
    }
  }

  /** Handler that sorts a list in place using a comparator registered per column. */
  public static class ListHandler<T> implements Handler {

    private final Map<Column<?, ?>, Comparator<T>> comparators = new HashMap<>();
    private List<T> list;

    public ListHandler(final List<T> list) {
      this.list = list;
    }

    public void setComparator(final Column<T, ?> column, final Comparator<T> comparator) {
      if (comparator == null) {
        comparators.remove(column);
      } else {
        comparators.put(column, comparator);
      }
    }

    public Comparator<T> getComparator(final Column<T, ?> column) {
      return comparators.get(column);
    }

    public List<T> getList() {
      return list;
    }

    public void setList(final List<T> list) {
      this.list = list;
    }

    @Override
    public void onColumnSort(final ColumnSortEvent event) {
      final Comparator<T> comparator = comparators.get(event.getColumn());
      if (comparator == null || list == null) {
        return;
      }
      Collections.sort(list, comparator);
      if (!event.isSortAscending()) {
        Collections.reverse(list);
      }
    }
  }

  private static final Type<Handler> TYPE = new Type<>();

  private final ColumnSortList sortList;

  protected ColumnSortEvent(final ColumnSortList sortList) {
    this.sortList = sortList;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public static ColumnSortEvent fire(final HasHandlers source, final ColumnSortList sortList) {
    final ColumnSortEvent event = new ColumnSortEvent(sortList);
    source.fireEvent(event);
    return event;
  }

  /** The most significant sorted column, or null when nothing is sorted. */
  public Column<?, ?> getColumn() {
    return sortList == null || sortList.size() == 0 ? null : sortList.get(0).getColumn();
  }

  public ColumnSortList getColumnSortList() {
    return sortList;
  }

  public boolean isSortAscending() {
    return sortList != null && sortList.size() > 0 && sortList.get(0).isAscending();
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final Handler handler) {
    handler.onColumnSort(this);
  }
}
