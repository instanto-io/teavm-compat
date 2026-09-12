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
package com.google.gwt.user.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * Browser event wrapper and the {@code ONXXX} bit constants. The library uses the constants for
 * sinkEvents-style bit masks and the type for native event payloads.
 */
public class Event extends NativeEvent {

  public static final int ONBLUR = 0x01000;
  public static final int ONCHANGE = 0x00400;
  public static final int ONCLICK = 0x00001;
  public static final int ONDBLCLICK = 0x00002;
  public static final int ONFOCUS = 0x00800;
  public static final int ONKEYDOWN = 0x00080;
  public static final int ONKEYPRESS = 0x00100;
  public static final int ONKEYUP = 0x00200;
  public static final int ONLOAD = 0x08000;
  public static final int ONMOUSEDOWN = 0x00004;
  public static final int ONMOUSEENTER = 0x100000;
  public static final int ONMOUSELEAVE = 0x200000;
  public static final int ONMOUSEMOVE = 0x00040;
  public static final int ONMOUSEOUT = 0x00020;
  public static final int ONMOUSEOVER = 0x00010;
  public static final int ONMOUSEUP = 0x00008;
  public static final int ONMOUSEWHEEL = 0x20000;
  public static final int ONPASTE = 0x80000;
  public static final int ONSCROLL = 0x04000;
  public static final int ONTOUCHSTART = 0x400000;
  public static final int ONTOUCHMOVE = 0x800000;
  public static final int ONTOUCHEND = 0x1000000;
  public static final int ONTOUCHCANCEL = 0x2000000;

  public static final int FOCUSEVENTS = ONFOCUS | ONBLUR;
  public static final int KEYEVENTS = ONKEYDOWN | ONKEYPRESS | ONKEYUP;
  public static final int MOUSEEVENTS =
      ONMOUSEDOWN | ONMOUSEUP | ONMOUSEMOVE | ONMOUSEOVER | ONMOUSEOUT;
  public static final int TOUCHEVENTS = ONTOUCHSTART | ONTOUCHMOVE | ONTOUCHEND | ONTOUCHCANCEL;
  public static final int UNDEFINED = 0;

  public Event(final org.teavm.jso.dom.events.Event event) {
    super(event);
  }

  /** Wraps a native event, or returns {@code null} for none. */
  public static Event as(final NativeEvent event) {
    return event == null ? null : new Event(event.unwrap());
  }

  /** Legacy GWT alias for the related mouse-event target. */
  public Element getRelatedTarget() {
    return Element.as(getRelatedEventTarget());
  }

  /**
   * The event mask previously sunk on an element. Handlers here attach real DOM listeners on
   * registration, so nothing is pre-sunk and this reports none.
   */
  /**
   * The bit constant for a DOM event name, as GWT's Event exposes it.
   *
   * <p>Widgets test the event they are given against the ON* constants, which needs the name
   * mapping back to a bit. Unknown names give -1, matching GWT.
   */
  public static int getTypeInt(final String eventType) {
    if (eventType == null) {
      return -1;
    }
    switch (eventType) {
      case "blur":
        return ONBLUR;
      case "change":
        return ONCHANGE;
      case "click":
        return ONCLICK;
      case "dblclick":
        return ONDBLCLICK;
      case "focus":
        return ONFOCUS;
      case "keydown":
        return ONKEYDOWN;
      case "keypress":
        return ONKEYPRESS;
      case "keyup":
        return ONKEYUP;
      case "load":
        return ONLOAD;
      case "mousedown":
        return ONMOUSEDOWN;
      case "mouseenter":
        return ONMOUSEENTER;
      case "mouseleave":
        return ONMOUSELEAVE;
      case "mousemove":
        return ONMOUSEMOVE;
      case "mouseout":
        return ONMOUSEOUT;
      case "mouseover":
        return ONMOUSEOVER;
      case "mouseup":
        return ONMOUSEUP;
      case "mousewheel":
        return ONMOUSEWHEEL;
      case "paste":
        return ONPASTE;
      case "scroll":
        return ONSCROLL;
      case "touchstart":
        return ONTOUCHSTART;
      default:
        return -1;
    }
  }

  public static int getEventsSunk(final com.google.gwt.dom.client.Element element) {
    return 0;
  }

  public static void sinkEvents(
      final com.google.gwt.dom.client.Element element, final int eventBits) {
    // No central dispatcher to register with; listeners bind on handler registration.
  }
}
