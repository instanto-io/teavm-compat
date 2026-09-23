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
package com.google.gwt.dom.client;

import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.MouseEvent;

/** Wraps the browser event behind the GWT {@code NativeEvent} surface. */
public class NativeEvent implements io.instanto.compat.NativeHandle {

  private final Event event;

  public NativeEvent(final Event event) {
    this.event = event;
  }

  public Event unwrap() {
    return event;
  }

  /** The event as GWT sees it, or null; used where native code returns an event. */
  public static NativeEvent wrap(final Event event) {
    return event == null ? null : new NativeEvent(event);
  }

  public String getType() {
    return event == null ? "" : event.getType();
  }

  public void preventDefault() {
    if (event != null) {
      event.preventDefault();
    }
  }

  public void stopPropagation() {
    if (event != null) {
      event.stopPropagation();
    }
  }

  public EventTarget getEventTarget() {
    return event == null ? null : wrap(event.getTarget());
  }

  public EventTarget getCurrentEventTarget() {
    return event == null ? null : wrap(event.getCurrentTarget());
  }

  public EventTarget getRelatedEventTarget() {
    return event instanceof MouseEvent ? wrap(((MouseEvent) event).getRelatedTarget()) : null;
  }

  private static EventTarget wrap(final org.teavm.jso.dom.events.EventTarget target) {
    return target == null ? null : new EventTarget(target);
  }

  public int getClientX() {
    return event instanceof MouseEvent ? ((MouseEvent) event).getClientX() : 0;
  }

  public int getClientY() {
    return event instanceof MouseEvent ? ((MouseEvent) event).getClientY() : 0;
  }

  public int getKeyCode() {
    return event == null ? 0 : keyCode(event);
  }

  public int getCharCode() {
    return event == null ? 0 : charCode(event);
  }

  public boolean getCtrlKey() {
    return event != null && modifier(event, "ctrlKey");
  }

  public boolean getShiftKey() {
    return event != null && modifier(event, "shiftKey");
  }

  public boolean getAltKey() {
    return event != null && modifier(event, "altKey");
  }

  public boolean getMetaKey() {
    return event != null && modifier(event, "metaKey");
  }

  @org.teavm.jso.JSBody(
      params = {"e"},
      script = "return e.keyCode || e.which || 0;")
  private static native int keyCode(Event e);

  @org.teavm.jso.JSBody(
      params = {"e"},
      script = "return e.charCode || 0;")
  private static native int charCode(Event e);

  @org.teavm.jso.JSBody(
      params = {"e", "name"},
      script = "return !!e[name];")
  private static native boolean modifier(Event e, String name);

  public com.google.gwt.core.client.JsArray<Touch> getTouches() {
    return new com.google.gwt.core.client.JsArray<>(touches(event, "touches"), Touch::new);
  }

  public com.google.gwt.core.client.JsArray<Touch> getChangedTouches() {
    return new com.google.gwt.core.client.JsArray<>(touches(event, "changedTouches"), Touch::new);
  }

  public com.google.gwt.core.client.JsArray<Touch> getTargetTouches() {
    return new com.google.gwt.core.client.JsArray<>(touches(event, "targetTouches"), Touch::new);
  }

  public double getScale() {
    return gesture(event, "scale");
  }

  public double getRotation() {
    return gesture(event, "rotation");
  }

  @org.teavm.jso.JSBody(
      params = {"e", "name"},
      script = "return e[name];")
  private static native org.teavm.jso.JSObject touches(Event event, String name);

  @org.teavm.jso.JSBody(
      params = {"e", "name"},
      script = "return e[name];")
  private static native double gesture(Event event, String name);
}
