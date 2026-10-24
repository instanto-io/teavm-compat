package com.google.gwt.user.client.ui;

import com.google.gwt.event.shared.UmbrellaException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/** Runs every cleanup operation, reporting failures only after ownership has been released. */
final class Lifecycle {
  private Lifecycle() {}

  static <T> void forEach(Iterable<T> values, Consumer<T> action) {
    Set<Throwable> failures = new LinkedHashSet<>();
    for (T value : values) {
      try {
        action.accept(value);
      } catch (Throwable failure) {
        failures.add(failure);
      }
    }
    if (!failures.isEmpty()) throw new UmbrellaException(failures);
  }
}
