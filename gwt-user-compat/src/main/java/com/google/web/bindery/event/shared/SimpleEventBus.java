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
package com.google.web.bindery.event.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory event bus, sufficient for the widget library's validation events. */
public class SimpleEventBus extends EventBus {

  private final Map<Event.Type<?>, List<Object>> handlers = new HashMap<>();

  @Override
  public <H> HandlerRegistration addHandler(final Event.Type<H> type, final H handler) {
    handlers.computeIfAbsent(type, key -> new ArrayList<>()).add(handler);
    return () -> {
      final List<Object> registered = handlers.get(type);
      if (registered != null) {
        registered.remove(handler);
      }
    };
  }

  @Override
  public <H> HandlerRegistration addHandlerToSource(
      final Event.Type<H> type, final Object source, final H handler) {
    return addHandler(type, handler);
  }

  @Override
  public void fireEvent(final Event<?> event) {
    fireEventFromSource(event, null);
  }

  @Override
  public void fireEventFromSource(final Event<?> event, final Object source) {
    if (event == null) {
      return;
    }
    final List<Object> registered = handlers.get(event.getAssociatedType());
    if (registered == null) {
      return;
    }
    for (final Object handler : new ArrayList<>(registered)) {
      event.dispatchUnchecked(handler);
    }
  }
}
