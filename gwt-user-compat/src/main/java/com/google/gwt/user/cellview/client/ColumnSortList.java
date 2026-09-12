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

import java.util.ArrayList;
import java.util.List;

/**
 * The columns a view is sorted by, most significant first.
 *
 * <p>Pushing a column that is already at the front flips its direction; pushing any other column
 * moves it to the front, keeping the previous sorts behind it as tie-breakers.
 */
public class ColumnSortList {

  /** A column and the direction it is sorted in. */
  public static class ColumnSortInfo {

    private final Column<?, ?> column;
    private final boolean ascending;

    public ColumnSortInfo(final Column<?, ?> column, final boolean ascending) {
      this.column = column;
      this.ascending = ascending;
    }

    public Column<?, ?> getColumn() {
      return column;
    }

    public boolean isAscending() {
      return ascending;
    }

    @Override
    public boolean equals(final Object other) {
      if (!(other instanceof ColumnSortInfo)) {
        return false;
      }
      final ColumnSortInfo that = (ColumnSortInfo) other;
      return ascending == that.ascending
          && (column == null ? that.column == null : column.equals(that.column));
    }

    @Override
    public int hashCode() {
      return (column == null ? 0 : column.hashCode()) * 31 + (ascending ? 1 : 0);
    }
  }

  /** Notified when the list changes, so the view can re-render its headers. */
  public interface Delegate {
    void onModification();
  }

  private final List<ColumnSortInfo> infos = new ArrayList<>();
  private final Delegate delegate;

  public ColumnSortList() {
    this(null);
  }

  public ColumnSortList(final Delegate delegate) {
    this.delegate = delegate;
  }

  public void clear() {
    infos.clear();
    fireModification();
  }

  public ColumnSortInfo get(final int index) {
    return infos.get(index);
  }

  public int size() {
    return infos.size();
  }

  public boolean insert(final int index, final ColumnSortInfo info) {
    final boolean removed = removeInternal(info.getColumn());
    infos.add(Math.min(index, infos.size()), info);
    fireModification();
    return removed;
  }

  /** Pushes a column to the front, flipping its direction if it is already there. */
  public boolean push(final Column<?, ?> column) {
    boolean ascending = column.isDefaultSortAscending();
    if (!infos.isEmpty() && infos.get(0).getColumn() == column) {
      ascending = !infos.get(0).isAscending();
    }
    return push(new ColumnSortInfo(column, ascending));
  }

  public boolean push(final ColumnSortInfo info) {
    final boolean removed = removeInternal(info.getColumn());
    infos.add(0, info);
    fireModification();
    return removed;
  }

  public ColumnSortInfo remove(final int index) {
    final ColumnSortInfo removed = infos.remove(index);
    fireModification();
    return removed;
  }

  private boolean removeInternal(final Column<?, ?> column) {
    for (int i = 0; i < infos.size(); i++) {
      if (infos.get(i).getColumn() == column) {
        infos.remove(i);
        return true;
      }
    }
    return false;
  }

  private void fireModification() {
    if (delegate != null) {
      delegate.onModification();
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof ColumnSortList)) {
      return false;
    }
    return infos.equals(((ColumnSortList) other).infos);
  }

  @Override
  public int hashCode() {
    return infos.hashCode();
  }
}
