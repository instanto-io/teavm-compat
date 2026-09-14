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
package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/** Base class for events that originate from a browser event. */
public abstract class DomEvent<H extends EventHandler> extends GwtEvent<H> {

  /**
   * A DOM event type. It carries the browser event name used to attach the native listener, and a
   * factory so a fresh event instance can be created per dispatch.
   */
  public static class Type<H extends EventHandler> extends GwtEvent.Type<H> {

    /** Creates a new event instance for this type. */
    public interface Factory<H extends EventHandler> {
      DomEvent<H> create();
    }

    private final String name;
    private final Factory<H> factory;

    public Type(final String name, final Factory<H> factory) {
      this.name = name;
      this.factory = factory;
      registerType(this);
    }

    public String getName() {
      return name;
    }

    public DomEvent<H> createEvent() {
      return factory.create();
    }
  }

  private NativeEvent nativeEvent;
  private com.google.gwt.dom.client.Element relativeElement;

  public com.google.gwt.dom.client.Element getRelativeElement() {
    return relativeElement;
  }

  public void setRelativeElement(com.google.gwt.dom.client.Element element) {
    relativeElement = element;
  }

  public NativeEvent getNativeEvent() {
    return nativeEvent;
  }

  public void setNativeEvent(final NativeEvent nativeEvent) {
    this.nativeEvent = nativeEvent;
  }

  public void preventDefault() {
    if (nativeEvent != null) {
      nativeEvent.preventDefault();
    }
  }

  public void stopPropagation() {
    if (nativeEvent != null) {
      nativeEvent.stopPropagation();
    }
  }

  /** Builds the typed event for a native event and fires it at {@code source}. */
  public static void fireNativeEvent(final NativeEvent nativeEvent, final HasHandlers source) {
    fireNativeEvent(nativeEvent, source, null);
  }

  public static void fireNativeEvent(
      NativeEvent nativeEvent,
      HasHandlers source,
      com.google.gwt.dom.client.Element relativeElement) {
    if (nativeEvent == null || source == null) {
      return;
    }
    final Type<?> type = TYPES.get(nativeEvent.getType());
    if (type == null) {
      return;
    }
    final DomEvent<?> event = type.createEvent();
    event.setNativeEvent(nativeEvent);
    event.setRelativeElement(relativeElement);
    source.fireEvent(event);
  }

  private static final java.util.Map<String, Type<?>> TYPES = new java.util.HashMap<>();

  static void registerType(final Type<?> type) {
    TYPES.put(type.getName(), type);
  }
}
