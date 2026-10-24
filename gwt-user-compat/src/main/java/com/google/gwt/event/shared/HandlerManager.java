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

/** Dispatches events with deferred handler mutations and aggregated failures. */
public class HandlerManager implements HasHandlers {
  private final Bus bus;
  private final Object source;

  private static final class Bus extends com.google.web.bindery.event.shared.SimpleEventBus {
    Bus(boolean reverse) {
      super(reverse);
    }

    <H> void remove(GwtEvent.Type<H> type, H handler) {
      doRemove(type, null, handler);
    }

    int count(GwtEvent.Type<?> type) {
      return getHandlerCount(type);
    }

    <H> H handler(GwtEvent.Type<H> type, int index) {
      return getHandler(type, index);
    }
  }

  public HandlerManager(Object source) {
    this(source, false);
  }

  public HandlerManager(Object source, boolean fireInReverseOrder) {
    this.source = source;
    bus = new Bus(fireInReverseOrder);
  }

  public <H extends EventHandler> HandlerRegistration addHandler(GwtEvent.Type<H> type, H handler) {
    var registration = bus.addHandler(type, handler);
    return registration::removeHandler;
  }

  public <H extends EventHandler> void removeHandler(GwtEvent.Type<H> type, H handler) {
    bus.remove(type, handler);
  }

  public int getHandlerCount(GwtEvent.Type<?> type) {
    return bus.count(type);
  }

  public <H extends EventHandler> H getHandler(GwtEvent.Type<H> type, int index) {
    return bus.handler(type, index);
  }

  public boolean isEventHandled(GwtEvent.Type<?> type) {
    return getHandlerCount(type) != 0;
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    java.util.Objects.requireNonNull(event, "event");
    Object previous = event.getSource();
    event.doSetSource(source);
    try {
      bus.fireEvent(event);
    } catch (com.google.web.bindery.event.shared.UmbrellaException failure) {
      throw new UmbrellaException(failure.getCauses());
    } finally {
      event.doSetSource(previous);
    }
  }
}
