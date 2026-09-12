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
package com.google.gwt.view.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the rows in a list and pushes the visible slice to each attached view.
 *
 * <p>Mutate the list returned by {@link #getList()} and call {@link #refresh()}, or use {@link
 * #setList(List)}. GWT's version wraps the list so mutations flush automatically; here the flush is
 * explicit, so a batch of edits costs one render.
 */
public class ListDataProvider<T> extends AbstractDataProvider<T> {

  private List<T> list;

  public ListDataProvider() {
    this(new ArrayList<>(), null);
  }

  public ListDataProvider(final List<T> list) {
    this(list, null);
  }

  public ListDataProvider(final ProvidesKey<T> keyProvider) {
    this(new ArrayList<>(), keyProvider);
  }

  public ListDataProvider(final List<T> list, final ProvidesKey<T> keyProvider) {
    super(keyProvider);
    this.list = list == null ? new ArrayList<>() : list;
    updateRowCount(this.list.size(), true);
  }

  public List<T> getList() {
    return list;
  }

  public void setList(final List<T> list) {
    this.list = list == null ? new ArrayList<>() : list;
    refresh();
  }

  /** Pushes the current list to every attached view. */
  public void refresh() {
    updateRowCount(list.size(), true);
    for (final HasData<T> display : getDataDisplays()) {
      onRangeChanged(display);
    }
  }

  public void flush() {
    refresh();
  }

  @Override
  protected void onRangeChanged(final HasData<T> display) {
    final Range range = display.getVisibleRange();
    final int start = Math.max(0, range.getStart());
    final int end = Math.min(list.size(), start + range.getLength());
    final List<T> slice =
        start >= end ? Collections.<T>emptyList() : new ArrayList<>(list.subList(start, end));
    updateRowData(display, start, slice);
  }
}
