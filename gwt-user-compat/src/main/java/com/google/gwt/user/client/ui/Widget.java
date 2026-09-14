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
package com.google.gwt.user.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import java.util.HashMap;
import java.util.Map;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.Registration;

/**
 * Element-backed widget with GWT's handler-registration surface, implemented over TeaVM DOM events.
 */
public class Widget extends UIObject
    implements IsWidget,
        HasHandlers,
        HasClickHandlers,
        HasDoubleClickHandlers,
        com.google.gwt.event.logical.shared.HasAttachHandlers {

  private Widget parent;
  private HandlerManager handlerManager;
  private boolean attached;

  /** One native listener per DOM event name; it re-dispatches through the handler manager. */
  private final Map<String, Registration> nativeListeners = new HashMap<>();

  @Override
  public Widget asWidget() {
    return this;
  }

  public Widget getParent() {
    return parent;
  }

  void setParent(final Widget parent) {
    final Widget oldParent = this.parent;
    if (parent == null) {
      if (oldParent != null && oldParent.isAttached()) {
        onDetach();
      }
      this.parent = null;
    } else {
      if (oldParent != null) {
        throw new IllegalStateException(
            "Cannot set a new parent without first clearing the old parent");
      }
      this.parent = parent;
      if (parent.isAttached()) {
        onAttach();
      }
    }
  }

  public boolean isAttached() {
    return attached;
  }

  /** True once this widget has been attached, whether or not it still is. */
  protected boolean isOrWasAttached() {
    return attached || everAttached;
  }

  private boolean everAttached;

  /** Receives a browser event before dispatching it to this widget's handlers. */
  public void onBrowserEvent(final com.google.gwt.user.client.Event event) {
    DomEvent.fireNativeEvent(event, this);
  }

  protected void onEnsureDebugId(final String baseId) {
    getElement().setId(baseId);
  }

  protected void onAttach() {
    attached = true;
    everAttached = true;
    doAttachChildren();
    onLoad();
    com.google.gwt.event.logical.shared.AttachEvent.fire(this, true);
  }

  protected void onDetach() {
    try {
      onUnload();
      com.google.gwt.event.logical.shared.AttachEvent.fire(this, false);
    } finally {
      try {
        doDetachChildren();
      } finally {
        attached = false;
      }
    }
  }

  /** Attaches children before this widget's load notification. */
  protected void doAttachChildren() {}

  /** Detaches children after this widget's unload notification. */
  protected void doDetachChildren() {}

  /** Called after this widget is attached to the document. */
  protected void onLoad() {}

  /** Called before this widget is detached from the document. */
  protected void onUnload() {}

  /** Registers for attach/detach notifications on this widget. */
  public HandlerRegistration addAttachHandler(
      final com.google.gwt.event.logical.shared.AttachEvent.Handler handler) {
    return addHandler(handler, com.google.gwt.event.logical.shared.AttachEvent.getType());
  }

  /** Routes an event to a widget's handler manager, as GWT's static helper does. */
  public static void delegateEvent(final Widget target, final GwtEvent<?> event) {
    if (target != null) {
      target.fireEvent(event);
    }
  }

  public void removeFromParent() {
    if (parent instanceof HasWidgets) {
      ((HasWidgets) parent).remove(this);
    } else if (getElement() != null) {
      getElement().removeFromParent();
      parent = null;
    }
  }

  protected HandlerManager ensureHandlers() {
    if (handlerManager == null) {
      handlerManager = new HandlerManager(this);
    }
    return handlerManager;
  }

  public <H extends EventHandler> HandlerRegistration addHandler(
      final H handler, final GwtEvent.Type<H> type) {
    return ensureHandlers().addHandler(type, handler);
  }

  /**
   * Registers a handler for a browser event, attaching the underlying native listener on first use
   * for that event name.
   */
  public <H extends EventHandler> HandlerRegistration addDomHandler(
      final H handler, final DomEvent.Type<H> type) {
    if (handler == null) {
      throw new IllegalArgumentException("handler must not be null");
    }
    ensureNativeListener(type);
    return ensureHandlers().addHandler(type, handler);
  }

  private <H extends EventHandler> void ensureNativeListener(final DomEvent.Type<H> type) {
    ensureNativeListener(type.getName());
  }

  private void ensureNativeListener(String name) {
    if (nativeListeners.containsKey(name)) {
      return;
    }
    final EventListener<Event> listener =
        nativeEvent -> {
          onBrowserEvent(new com.google.gwt.user.client.Event(nativeEvent));
        };
    nativeListeners.put(name, getElement().unwrap().onEvent(name, listener));
  }

  @Override
  public void fireEvent(final GwtEvent<?> event) {
    if (handlerManager != null) {
      handlerManager.fireEvent(event);
    }
  }

  @Override
  public HandlerRegistration addClickHandler(final ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  @Override
  public HandlerRegistration addDoubleClickHandler(final DoubleClickHandler handler) {
    return addDomHandler(handler, DoubleClickEvent.getType());
  }

  public void sinkBitlessEvent(String eventName) {
    ensureNativeListener(eventName);
  }
}
