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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Feeds row data to one or more views. */
public abstract class AbstractDataProvider<T> {

  private final Set<HasData<T>> displays = new HashSet<>();
  private ProvidesKey<T> keyProvider;
  private int rowCount = -1;
  private boolean rowCountExact;

  protected AbstractDataProvider() {
    this(null);
  }

  protected AbstractDataProvider(final ProvidesKey<T> keyProvider) {
    this.keyProvider = keyProvider;
  }

  public void addDataDisplay(final HasData<T> display) {
    if (display == null) {
      throw new IllegalArgumentException("display must not be null");
    }
    displays.add(display);
    if (rowCount >= 0) {
      display.setRowCount(rowCount, rowCountExact);
    }
    onRangeChanged(display);
    display.addRangeChangeHandler(event -> onRangeChanged(display));
  }

  public void removeDataDisplay(final HasData<T> display) {
    displays.remove(display);
  }

  public Set<HasData<T>> getDataDisplays() {
    return new HashSet<>(displays);
  }

  public ProvidesKey<T> getKeyProvider() {
    return keyProvider;
  }

  public Object getKey(final T item) {
    return keyProvider == null ? item : keyProvider.getKey(item);
  }

  public int getRowCount() {
    return rowCount;
  }

  public boolean isRowCountExact() {
    return rowCountExact;
  }

  protected void updateRowCount(final int count, final boolean exact) {
    rowCount = count;
    rowCountExact = exact;
    for (final HasData<T> display : displays) {
      display.setRowCount(count, exact);
    }
  }

  protected void updateRowData(final int start, final List<T> values) {
    for (final HasData<T> display : displays) {
      display.setRowData(start, values);
    }
  }

  protected void updateRowData(final HasData<T> display, final int start, final List<T> values) {
    display.setRowData(start, values);
  }

  /** Called when a view's visible range changes and needs data. */
  protected abstract void onRangeChanged(HasData<T> display);
}
