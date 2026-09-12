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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The panels that wrap elements already on the page.
 *
 * <p>Two things here are less obvious than they look, and both were missing.
 *
 * <p>A panel is cached per element, because it owns the widgets added to it. Returning a fresh
 * panel for a second call on the same id would hand back something that shares an element with the
 * first but knows nothing of its children: clearing one would leave the other's widgets on the
 * page, and attaching would be announced twice.
 *
 * <p>Widgets can also be registered for detachment when the page goes away, which is what gives
 * them a last onUnload -- the moment a widget releases a plugin, a timer or a listener it put
 * somewhere outside its own element.
 */
public final class RootPanel extends ComplexPanel {

  private static final Map<String, RootPanel> ROOTS = new HashMap<>();
  private static final Set<Widget> TO_DETACH = new LinkedHashSet<>();
  private static RootPanel bodyRoot;
  private static boolean closeHooked;

  private RootPanel(final Element element) {
    setElement(element);
    onAttach();
  }

  /** The panel wrapping the document body. */
  public static RootPanel get() {
    if (bodyRoot == null) {
      bodyRoot = new RootPanel(Document.get().getBody());
      detachOnWindowClose(bodyRoot);
    }
    return bodyRoot;
  }

  /**
   * The panel wrapping the element with this id, or null if there is no such element.
   *
   * <p>The same panel each time, so long as the element is: a page that replaces an element under
   * an id it reuses gets a new panel rather than one whose widgets belong to markup that has gone.
   */
  public static RootPanel get(final String id) {
    final Element element = Document.get().getElementById(id);
    if (element == null) {
      return null;
    }
    final RootPanel existing = ROOTS.get(id);
    if (existing != null && existing.getElement().unwrap() == element.unwrap()) {
      return existing;
    }
    final RootPanel created = new RootPanel(element);
    ROOTS.put(id, created);
    // A panel wrapping an element has no parent widget to detach it, so it asks
    // to be detached when the page closes, as GWT's does.
    detachOnWindowClose(created);
    return created;
  }

  /**
   * Asks for this widget to be detached when the page closes.
   *
   * <p>For a widget with no parent widget to detach it -- a panel wrapping an element already on
   * the page, typically. GWT asserts against registering a widget that has a parent, and against
   * registering twice; with assertions off both are harmless, and this behaves as that does.
   */
  public static void detachOnWindowClose(final Widget widget) {
    if (widget == null) {
      throw new IllegalArgumentException("widget must not be null");
    }
    TO_DETACH.add(widget);
    hookWindowClose();
  }

  /** Whether this widget will be detached when the page closes. */
  public static boolean isInDetachList(final Widget widget) {
    return TO_DETACH.contains(widget);
  }

  /** Detaches a registered widget now, and stops it being detached again later. */
  public static void detachNow(final Widget widget) {
    try {
      detach(widget);
    } finally {
      TO_DETACH.remove(widget);
    }
  }

  /** Detaches everything registered. Called as the page closes. */
  public static void detachWidgets() {
    for (final Widget widget : new LinkedHashSet<>(TO_DETACH)) {
      detach(widget);
    }
    TO_DETACH.clear();
  }

  private static void detach(final Widget widget) {
    if (widget.isAttached()) {
      widget.onDetach();
    }
  }

  private static void hookWindowClose() {
    if (closeHooked) {
      return;
    }
    closeHooked = true;
    onWindowClose((JsAction) RootPanel::detachWidgets);
  }

  /** The shape a JSBody callback can invoke, as elsewhere in this layer. */
  private interface JsAction extends org.teavm.jso.JSObject {
    void run();
  }

  @org.teavm.jso.JSBody(
      params = {"handler"},
      script = "window.addEventListener('unload', function () { handler.run(); });")
  private static native void onWindowClose(JsAction handler);
}
