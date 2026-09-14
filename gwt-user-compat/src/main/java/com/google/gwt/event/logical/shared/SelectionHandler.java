package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.EventHandler;

public interface SelectionHandler<T> extends EventHandler {
  void onSelection(SelectionEvent<T> event);
}
