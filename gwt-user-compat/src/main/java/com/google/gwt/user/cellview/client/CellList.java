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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A single-column list of values, each rendered by one {@link Cell}.
 *
 * <p>Each value becomes a child {@code div}. Events land on the child they occurred in and are
 * routed to the cell with the matching {@link Cell.Context}.
 */
public class CellList<T> extends AbstractHasData<T> {

  /** GWT's default page size. */
  public static final int DEFAULT_PAGESIZE = 25;

  /** Style names a list applies to its parts. */
  public interface Style extends com.google.gwt.resources.client.CssResource {
    String cellListEvenItem();

    String cellListKeyboardSelectedItem();

    String cellListOddItem();

    String cellListSelectedItem();

    String cellListWidget();
  }

  /** Images and styles a list uses. */
  public interface Resources extends com.google.gwt.resources.client.ClientBundle {
    com.google.gwt.resources.client.ImageResource cellListSelectedBackground();

    Style cellListStyle();
  }

  private final Cell<T> cell;
  private final Element container;
  private final Set<String> boundEvents = new HashSet<>();
  private Widget emptyListWidget;
  private String emptyListText = "";
  private boolean listenersBound;

  public CellList(final Cell<T> cell) {
    this(cell, DEFAULT_PAGESIZE, null);
  }

  public CellList(final Cell<T> cell, final ProvidesKey<T> keyProvider) {
    this(cell, DEFAULT_PAGESIZE, keyProvider);
  }

  public CellList(final Cell<T> cell, final Resources resources) {
    this(cell, DEFAULT_PAGESIZE, null);
  }

  public CellList(final Cell<T> cell, final int pageSize, final ProvidesKey<T> keyProvider) {
    super(pageSize, keyProvider);
    this.cell = cell;
    this.container = Document.get().createDivElement();
    setElement(container);
    setStyleName("cellList");
  }

  public Cell<T> getCell() {
    return cell;
  }

  public void setEmptyListText(final String text) {
    emptyListText = text == null ? "" : text;
    refresh();
  }

  public Widget getEmptyListWidget() {
    return emptyListWidget;
  }

  public void setEmptyListWidget(final Widget widget) {
    emptyListWidget = widget;
    refresh();
  }

  /** Style put on the element of a selected value. */
  protected String getSelectedItemStyle() {
    return "selected";
  }

  @Override
  protected void render() {
    ensureListeners();
    final List<T> rows = getRowData();
    if (rows.isEmpty()) {
      final SafeHtmlBuilder empty = new SafeHtmlBuilder();
      empty.appendEscaped(emptyListText);
      container.setInnerSafeHtml(empty.toSafeHtml());
      return;
    }

    final SafeHtmlBuilder sb = new SafeHtmlBuilder();
    final int pageStart = getPageStart();
    for (int i = 0; i < rows.size(); i++) {
      final T value = rows.get(i);
      final int absoluteIndex = pageStart + i;
      sb.appendHtmlConstant(
          "<div" + (isRowSelected(value) ? " class=\"" + getSelectedItemStyle() + "\"" : "") + ">");
      cell.render(new Cell.Context(absoluteIndex, 0, getRowKey(value)), value, sb);
      sb.appendHtmlConstant("</div>");
    }
    container.setInnerSafeHtml(sb.toSafeHtml());
  }

  private void ensureListeners() {
    if (listenersBound) {
      return;
    }
    listenersBound = true;
    final Set<String> events = cell.getConsumedEvents();
    final Set<String> wanted = new HashSet<>();
    if (events != null) {
      wanted.addAll(events);
    }
    wanted.add("click");
    for (final String eventName : wanted) {
      if (boundEvents.add(eventName)) {
        container.unwrap().addEventListener(eventName, this::dispatch);
      }
    }
  }

  private void dispatch(final org.teavm.jso.dom.events.Event nativeEvent) {
    final NativeEvent event = new NativeEvent(nativeEvent);
    final Element target = Element.as(event.getEventTarget());
    if (target == null) {
      return;
    }
    final Element item = directChildContaining(target);
    if (item == null) {
      return;
    }
    final int index = indexOfChild(container, item);
    if (index < 0 || index >= getRowData().size()) {
      return;
    }

    final T value = getRowData().get(index);
    final int absoluteIndex = getPageStart() + index;
    final Cell.Context context = new Cell.Context(absoluteIndex, 0, getRowKey(value));

    final CellPreviewEvent<T> preview =
        CellPreviewEvent.fire(
            this,
            event,
            this,
            absoluteIndex,
            0,
            value,
            cell.isEditing(context, item, value),
            cell.handlesSelection());
    if (preview.isCanceled()) {
      return;
    }

    cell.onBrowserEvent(context, item, value, event, null);

    final SelectionModel<? super T> selectionModel = getSelectionModel();
    if (selectionModel != null && "click".equals(event.getType()) && !cell.handlesSelection()) {
      selectionModel.setSelected(value, !selectionModel.isSelected(value));
    }
  }

  /** Walks up from the event target to the direct child of the list container. */
  private Element directChildContaining(final Element target) {
    Element current = target;
    while (current != null) {
      final Element parent = current.getParentElement();
      if (parent != null && parent.unwrap() == container.unwrap()) {
        return current;
      }
      current = parent;
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
