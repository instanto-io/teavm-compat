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
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

/** Describes the children of each node in a tree, and how to render them. */
public interface TreeViewModel {

  /** How to obtain and render one node's children. */
  interface NodeInfo<T> {

    Cell<T> getCell();

    ProvidesKey<T> getProvidesKey();

    SelectionModel<? super T> getSelectionModel();

    ValueUpdater<T> getValueUpdater();

    /** Attaches a view that will display these children. */
    void setDataDisplay(HasData<T> display);

    void unsetDataDisplay();
  }

  /** The children of {@code value}, or null when it has none. */
  <T> NodeInfo<?> getNodeInfo(T value);

  /** True when {@code value} can have no children. */
  boolean isLeaf(Object value);

  /** A {@link NodeInfo} backed by a data provider. */
  class DefaultNodeInfo<T> implements NodeInfo<T> {

    private final AbstractDataProvider<T> dataProvider;
    private final Cell<T> cell;
    private final SelectionModel<? super T> selectionModel;
    private final ValueUpdater<T> valueUpdater;
    private HasData<T> display;

    public DefaultNodeInfo(final AbstractDataProvider<T> dataProvider, final Cell<T> cell) {
      this(dataProvider, cell, null, null);
    }

    public DefaultNodeInfo(
        final AbstractDataProvider<T> dataProvider,
        final Cell<T> cell,
        final SelectionModel<? super T> selectionModel,
        final ValueUpdater<T> valueUpdater) {
      this.dataProvider = dataProvider;
      this.cell = cell;
      this.selectionModel = selectionModel;
      this.valueUpdater = valueUpdater;
    }

    @Override
    public Cell<T> getCell() {
      return cell;
    }

    @Override
    public ProvidesKey<T> getProvidesKey() {
      return dataProvider == null ? null : dataProvider.getKeyProvider();
    }

    @Override
    public SelectionModel<? super T> getSelectionModel() {
      return selectionModel;
    }

    @Override
    public ValueUpdater<T> getValueUpdater() {
      return valueUpdater;
    }

    public AbstractDataProvider<T> getProvider() {
      return dataProvider;
    }

    @Override
    public void setDataDisplay(final HasData<T> display) {
      this.display = display;
      if (dataProvider != null) {
        dataProvider.addDataDisplay(display);
      }
    }

    @Override
    public void unsetDataDisplay() {
      if (dataProvider != null && display != null) {
        dataProvider.removeDataDisplay(display);
      }
      display = null;
    }
  }
}
