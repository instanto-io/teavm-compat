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

/** Browser window geometry and scrolling. */
public final class Window {

  private Window() {}

  public static int getClientWidth() {
    return clientWidth();
  }

  public static int getClientHeight() {
    return clientHeight();
  }

  public static int getScrollLeft() {
    return scrollLeft();
  }

  public static int getScrollTop() {
    return scrollTop();
  }

  public static void alert(final String message) {
    org.teavm.jso.browser.Window.alert(message);
  }

  @org.teavm.jso.JSBody(script = "return document.documentElement.clientWidth | 0;")
  private static native int clientWidth();

  @org.teavm.jso.JSBody(script = "return document.documentElement.clientHeight | 0;")
  private static native int clientHeight();

  @org.teavm.jso.JSBody(script = "return (window.pageXOffset || 0) | 0;")
  private static native int scrollLeft();

  @org.teavm.jso.JSBody(script = "return (window.pageYOffset || 0) | 0;")
  private static native int scrollTop();

  /** Registers for window resize notifications. */
  public static com.google.gwt.event.shared.HandlerRegistration addResizeHandler(
      final com.google.gwt.event.logical.shared.ResizeHandler handler) {
    if (handler == null) {
      throw new IllegalArgumentException("handler must not be null");
    }
    final org.teavm.jso.dom.events.EventListener<org.teavm.jso.dom.events.Event> listener =
        event -> handler.onResize(new ResizeEventImpl(getClientWidth(), getClientHeight()));
    final org.teavm.jso.dom.events.Registration registration =
        org.teavm.jso.browser.Window.current().onEvent("resize", listener);
    return registration::dispose;
  }

  /** Concrete ResizeEvent, since the event's constructor is protected. */
  private static final class ResizeEventImpl
      extends com.google.gwt.event.logical.shared.ResizeEvent {
    ResizeEventImpl(final int width, final int height) {
      super(width, height);
    }
  }

  /** Scrolls the page to an absolute position, as the showcase does on navigation. */
  public static void scrollTo(final int left, final int top) {
    scrollWindowTo(left, top);
  }

  @org.teavm.jso.JSBody(
      params = {"left", "top"},
      script = "window.scrollTo(left, top);")
  private static native void scrollWindowTo(int left, int top);
}
