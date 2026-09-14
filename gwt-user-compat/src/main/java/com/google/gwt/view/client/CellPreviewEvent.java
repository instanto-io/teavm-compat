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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Fired before a cell handles a browser event, so a handler can inspect or cancel it. */
public class CellPreviewEvent<T> extends GwtEvent<CellPreviewEvent.Handler<T>> {

  /** Receives cell previews. */
  public interface Handler<T> extends EventHandler {
    void onCellPreview(CellPreviewEvent<T> event);
  }

  private static final Type<Handler<?>> TYPE = new Type<>();

  private final NativeEvent nativeEvent;
  private final HasData<T> display;
  private final int index;
  private final int column;
  private final T value;
  private final boolean isCellEditing;
  private final boolean isSelectionHandled;
  private boolean canceled;

  protected CellPreviewEvent(
      final NativeEvent nativeEvent,
      final HasData<T> display,
      final int index,
      final int column,
      final T value,
      final boolean isCellEditing,
      final boolean isSelectionHandled) {
    this.nativeEvent = nativeEvent;
    this.display = display;
    this.index = index;
    this.column = column;
    this.value = value;
    this.isCellEditing = isCellEditing;
    this.isSelectionHandled = isSelectionHandled;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> Type<Handler<T>> getType() {
    return (Type) TYPE;
  }

  public static <T> CellPreviewEvent<T> fire(
      final HasCellPreviewHandlers<T> source,
      final NativeEvent nativeEvent,
      final HasData<T> display,
      final int index,
      final int column,
      final T value,
      final boolean isCellEditing,
      final boolean isSelectionHandled) {
    final CellPreviewEvent<T> event =
        new CellPreviewEvent<>(
            nativeEvent, display, index, column, value, isCellEditing, isSelectionHandled);
    source.fireEvent(event);
    return event;
  }

  public int getColumn() {
    return column;
  }

  public HasData<T> getDisplay() {
    return display;
  }

  public int getIndex() {
    return index;
  }

  public NativeEvent getNativeEvent() {
    return nativeEvent;
  }

  public T getValue() {
    return value;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public boolean isCellEditing() {
    return isCellEditing;
  }

  public boolean isSelectionHandled() {
    return isSelectionHandled;
  }

  public void setCanceled(final boolean canceled) {
    this.canceled = canceled;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Type<Handler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  @Override
  protected void dispatch(final Handler<T> handler) {
    handler.onCellPreview(this);
  }
}
