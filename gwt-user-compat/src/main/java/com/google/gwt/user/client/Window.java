/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * Copyright 2008 Google Inc.
 * %%
 * Reimplements, over TeaVM's JSO libraries, part of the GWT client API. Class,
 * method and package names follow GWT (https://github.com/gwtproject/gwt),
 * Copyright (C) The GWT Project Authors, licensed under the Apache License,
 * Version 2.0. Scroll event types retain the upstream implementation.
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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import org.teavm.jso.JSBody;

/** Browser window geometry, location and scrolling. */
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

  /** Browser location operations; values retain their native URL encoding. */
  public static final class Location {
    private Location() {}

    @JSBody(script = "return window.location.hash;")
    public static native String getHash();

    @JSBody(script = "return window.location.host;")
    public static native String getHost();

    @JSBody(script = "return window.location.hostname;")
    public static native String getHostName();

    @JSBody(script = "return window.location.href;")
    public static native String getHref();

    @JSBody(script = "return window.location.pathname;")
    public static native String getPath();

    @JSBody(script = "return window.location.port;")
    public static native String getPort();

    @JSBody(script = "return window.location.protocol;")
    public static native String getProtocol();

    @JSBody(script = "return window.location.search;")
    public static native String getQueryString();

    @JSBody(params = "url", script = "window.location.assign(url);")
    public static native void assign(String url);

    @JSBody(params = "url", script = "window.location.replace(url);")
    public static native void replace(String url);

    @JSBody(script = "window.location.reload();")
    public static native void reload();
  }

  /** Browser identification used by upstream feature checks. */
  public static final class Navigator {
    private Navigator() {}

    @JSBody(script = "return window.navigator.appCodeName;")
    public static native String getAppCodeName();

    @JSBody(script = "return window.navigator.appName;")
    public static native String getAppName();

    @JSBody(script = "return window.navigator.appVersion;")
    public static native String getAppVersion();

    @JSBody(script = "return window.navigator.platform;")
    public static native String getPlatform();

    @JSBody(script = "return window.navigator.userAgent;")
    public static native String getUserAgent();
  }

  public static class ScrollEvent extends GwtEvent<Window.ScrollHandler> {
    /** The event type. */
    static final Type<Window.ScrollHandler> TYPE = new Type<Window.ScrollHandler>();

    static Type<Window.ScrollHandler> getType() {
      return TYPE;
    }

    private int scrollLeft;
    private int scrollTop;

    /**
     * Construct a new {@link Window.ScrollEvent}.
     *
     * @param scrollLeft the left scroll position
     * @param scrollTop the top scroll position
     */
    private ScrollEvent(int scrollLeft, int scrollTop) {
      this.scrollLeft = scrollLeft;
      this.scrollTop = scrollTop;
    }

    @Override
    public final Type<ScrollHandler> getAssociatedType() {
      return TYPE;
    }

    /**
     * Gets the window's scroll left.
     *
     * @return window's scroll left
     */
    public int getScrollLeft() {
      return scrollLeft;
    }

    /**
     * Get the window's scroll top.
     *
     * @return the window's scroll top
     */
    public int getScrollTop() {
      return scrollTop;
    }

    @Override
    protected void dispatch(ScrollHandler handler) {
      handler.onWindowScroll(this);
    }
  }

  /** Handler for {@link Window.ScrollEvent} events. */
  public interface ScrollHandler extends EventHandler {
    /**
     * Fired when the browser window is scrolled.
     *
     * @param event the event
     */
    void onWindowScroll(Window.ScrollEvent event);
  }

  private static final SimpleEventBus SCROLL_HANDLERS = new SimpleEventBus();
  private static boolean scrollInitialized;

  /** Registers a window scroll handler; disposal removes it from future delivery. */
  public static HandlerRegistration addWindowScrollHandler(ScrollHandler handler) {
    if (!scrollInitialized) {
      org.teavm.jso.browser.Window.current()
          .onEvent(
              "scroll",
              event -> SCROLL_HANDLERS.fireEvent(new ScrollEvent(getScrollLeft(), getScrollTop())));
      scrollInitialized = true;
    }
    return SCROLL_HANDLERS.addHandler(ScrollEvent.TYPE, handler);
  }
}
