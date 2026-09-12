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
package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;

/**
 * A panel that scrolls its single child.
 *
 * <p>GWT's implementation carries workarounds for browsers that are no longer shipped, and reads
 * scroll offsets through a DOM implementation chosen by deferred binding. What remains once those
 * are gone is a div with overflow set, which is what this is.
 */
public class ScrollPanel extends SimplePanel {

  public ScrollPanel() {
    setElement(Document.get().createDivElement());
    getElement().getStyle().setProperty("overflow", "auto");
    getElement().getStyle().setProperty("position", "relative");
  }

  public ScrollPanel(final Widget child) {
    this();
    setWidget(child);
  }

  /** Whether the panel scrolls, or clips its content instead. */
  public void setAlwaysShowScrollBars(final boolean alwaysShow) {
    getElement().getStyle().setProperty("overflow", alwaysShow ? "scroll" : "auto");
  }

  public int getHorizontalScrollPosition() {
    return getElement().getScrollLeft();
  }

  public void setHorizontalScrollPosition(final int position) {
    getElement().setScrollLeft(position);
  }

  public int getVerticalScrollPosition() {
    return getElement().getScrollTop();
  }

  public void setVerticalScrollPosition(final int position) {
    getElement().setScrollTop(position);
  }

  /** Scrolls back to the origin, as GWT's does. */
  public void scrollToTop() {
    setVerticalScrollPosition(0);
  }

  public void scrollToLeft() {
    setHorizontalScrollPosition(0);
  }
}
