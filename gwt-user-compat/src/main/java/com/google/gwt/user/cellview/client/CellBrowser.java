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
import com.google.gwt.dom.client.Element;
import com.google.gwt.view.client.SingleSelectionModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A tree shown as side-by-side columns: selecting in one column opens the next.
 *
 * <p>Each column is a {@link CellList} over the children of the value selected in the column to its
 * left, so a {@link TreeViewModel} drives a browser exactly as it drives a {@link CellTree}. GWT
 * animates the horizontal scroll and supports keyboard navigation; neither is reproduced here.
 */
public class CellBrowser extends AbstractCellTree {

  /** GWT's default width for one column. */
  public static final int DEFAULT_COLUMN_WIDTH = 200;

  /** Style names a browser applies to its parts. */
  public interface Style extends com.google.gwt.resources.client.CssResource {
    String cellBrowserColumn();

    String cellBrowserFirstColumn();

    String cellBrowserKeyboardSelectedItem();

    String cellBrowserOpenItem();

    String cellBrowserSelectedItem();

    String cellBrowserWidget();
  }

  /** Images and styles a browser uses. */
  public interface Resources extends com.google.gwt.resources.client.ClientBundle {
    com.google.gwt.resources.client.ImageResource cellBrowserOpenBackground();

    com.google.gwt.resources.client.ImageResource cellBrowserSelectedBackground();

    Style cellBrowserStyle();
  }

  /** One column, and the value whose children it is showing. */
  private final class BrowserColumn<T> {

    private final Object parentValue;
    private final CellList<T> list;
    private final TreeViewModel.NodeInfo<T> nodeInfo;
    private final SingleSelectionModel<T> selection;

    BrowserColumn(final Object parentValue, final TreeViewModel.NodeInfo<T> nodeInfo) {
      this.parentValue = parentValue;
      this.nodeInfo = nodeInfo;
      this.selection = new SingleSelectionModel<>(nodeInfo.getProvidesKey());
      this.list = new CellList<>(nodeInfo.getCell(), defaultColumnSize, nodeInfo.getProvidesKey());
      this.list.setSelectionModel(selection);
      this.list.getElement().getStyle().setProperty("width", columnWidth + "px");
      this.list.getElement().getStyle().setProperty("display", "inline-block");
      this.list.getElement().getStyle().setProperty("vertical-align", "top");
      this.list.addStyleName("cellBrowserColumn");
      nodeInfo.setDataDisplay(list);
      selection.addSelectionChangeHandler(event -> openBelow(this));
    }

    Object selectedValue() {
      return selection.getSelectedObject();
    }
  }

  private final List<BrowserColumn<?>> columns = new ArrayList<>();
  private final Element container;
  private int columnWidth = DEFAULT_COLUMN_WIDTH;
  private int defaultColumnSize = CellList.DEFAULT_PAGESIZE;

  public CellBrowser(final TreeViewModel viewModel, final Object rootValue) {
    this(viewModel, rootValue, null);
  }

  public CellBrowser(
      final TreeViewModel viewModel, final Object rootValue, final Resources resources) {
    super(viewModel, rootValue);
    container = Document.get().createDivElement();
    container.getStyle().setProperty("white-space", "nowrap");
    container.getStyle().setProperty("overflow-x", "auto");
    setElement(container);
    setStyleName("cellBrowser");
    openColumnFor(rootValue);
  }

  public int getDefaultColumnWidth() {
    return columnWidth;
  }

  public void setDefaultColumnWidth(final int width) {
    this.columnWidth = width;
  }

  public int getDefaultNodeSize() {
    return defaultColumnSize;
  }

  public void setDefaultNodeSize(final int size) {
    this.defaultColumnSize = size;
  }

  @Override
  public TreeNode getRootTreeNode() {
    return null;
  }

  /** Drops every column right of {@code column}, then opens the new selection's children. */
  private void openBelow(final BrowserColumn<?> column) {
    final int index = columns.indexOf(column);
    if (index < 0) {
      return;
    }
    while (columns.size() > index + 1) {
      final BrowserColumn<?> removed = columns.remove(columns.size() - 1);
      removed.nodeInfo.unsetDataDisplay();
      removed.list.getElement().removeFromParent();
    }
    final Object selected = column.selectedValue();
    if (selected != null && !getTreeViewModel().isLeaf(selected)) {
      openColumnFor(selected);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void openColumnFor(final Object value) {
    final TreeViewModel.NodeInfo<?> info = getTreeViewModel().getNodeInfo(value);
    if (info == null) {
      return;
    }
    final BrowserColumn column = new BrowserColumn(value, (TreeViewModel.NodeInfo) info);
    columns.add(column);
    container.appendChild(column.list.getElement());
    column.list.onAttach();
  }
}
