package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.GwtEvent;

/** Selection notification matching the GWT handler and source contract. */
public class SelectionEvent<T> extends GwtEvent<SelectionHandler<T>> {
  private static final Type<SelectionHandler<?>> TYPE = new Type<>();
  private final T selectedItem;

  protected SelectionEvent(T selectedItem) {
    this.selectedItem = selectedItem;
  }

  public T getSelectedItem() {
    return selectedItem;
  }

  public static <T> void fire(HasSelectionHandlers<T> source, T selectedItem) {
    source.fireEvent(new SelectionEvent<>(selectedItem));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> Type<SelectionHandler<T>> getType() {
    return (Type) TYPE;
  }

  @Override
  public Type<SelectionHandler<T>> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(SelectionHandler<T> handler) {
    handler.onSelection(this);
  }
}
