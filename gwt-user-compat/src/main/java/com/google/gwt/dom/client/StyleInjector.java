/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
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

import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;

/**
 * Adds a stylesheet to the page, as GWT's does.
 *
 * <p>A UiBinder template's {@code <ui:style>} block becomes a call to this. GWT collects those
 * blocks into a CssResource and injects them; here the generated binder passes the text straight
 * through, which is the same effect for a template that declares plain rules rather than a typed
 * resource.
 */
public class StyleInjector {

  private StyleInjector() {}

  /** Appends a stylesheet to the document head. */
  public static void inject(final String css) {
    injectAtEnd(css);
  }

  /** Appends a stylesheet after any already injected, so later rules win. */
  public static void injectAtEnd(final String css) {
    if (css == null || css.isEmpty()) {
      return;
    }
    final HTMLDocument document = HTMLDocument.current();
    final HTMLElement style = document.createElement("style");
    style.setAttribute("type", "text/css");
    style.appendChild((Node) document.createTextNode(css));
    document.getHead().appendChild((Node) style);
  }

  /**
   * Appends a stylesheet, immediately when asked.
   *
   * <p>GWT batches injections into a single deferred command and takes {@code true} to mean "flush
   * now". There is no batching here, so both forms are immediate and the flag only decides whether
   * the caller can rely on the rules being live on return -- which, here, it always can.
   */
  public static void injectAtEnd(final String css, final boolean immediate) {
    injectAtEnd(css);
  }

  /** Prepends a stylesheet, so page rules can still override it. */
  public static void injectAtStart(final String css) {
    if (css == null || css.isEmpty()) {
      return;
    }
    final HTMLDocument document = HTMLDocument.current();
    final HTMLElement style = document.createElement("style");
    style.setAttribute("type", "text/css");
    style.appendChild((Node) document.createTextNode(css));
    final HTMLElement head = document.getHead();
    head.insertBefore((Node) style, head.getFirstChild());
  }
}
