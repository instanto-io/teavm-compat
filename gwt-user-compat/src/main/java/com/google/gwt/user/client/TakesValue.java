package com.google.gwt.user.client;

/** An object which exposes a value without requiring a rendered widget. */
public interface TakesValue<T> {
  T getValue();

  void setValue(T value);
}
