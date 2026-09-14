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
package com.google.gwt.event.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatches {@link GwtEvent}s to registered handlers.
 *
 * <p>Handlers are copied before dispatch so a handler may add or remove handlers while an event is
 * being delivered, matching GWT's documented behaviour.
 */
public class HandlerManager implements HasHandlers {

  private final Map<GwtEvent.Type<?>, List<EventHandler>> handlers = new HashMap<>();
  private final Object source;

  public HandlerManager(final Object source) {
    this.source = source;
  }

  public <H extends EventHandler> HandlerRegistration addHandler(
      final GwtEvent.Type<H> type, final H handler) {
    if (type == null) {
      throw new IllegalArgumentException("type must not be null");
    }
    if (handler == null) {
      throw new IllegalArgumentException("handler must not be null");
    }
    handlers.computeIfAbsent(type, key -> new ArrayList<>()).add(handler);
    return () -> removeHandler(type, handler);
  }

  public <H extends EventHandler> void removeHandler(final GwtEvent.Type<H> type, final H handler) {
    final List<EventHandler> registered = handlers.get(type);
    if (registered != null) {
      registered.remove(handler);
      if (registered.isEmpty()) {
        handlers.remove(type);
      }
    }
  }

  public int getHandlerCount(final GwtEvent.Type<?> type) {
    final List<EventHandler> registered = handlers.get(type);
    return registered == null ? 0 : registered.size();
  }

  @Override
  public void fireEvent(final GwtEvent<?> event) {
    if (event == null) {
      return;
    }
    final List<EventHandler> registered = handlers.get(event.getAssociatedType());
    if (registered == null || registered.isEmpty()) {
      return;
    }
    if (event.getSource() == null) {
      event.doSetSource(source);
    }
    for (final EventHandler handler : new ArrayList<>(registered)) {
      event.doDispatch(handler);
    }
  }
}
