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

import com.google.gwt.dom.client.Element;
import java.util.Iterator;

/**
 * Panel that holds an ordered collection of children, each attached to a container element. Mirrors
 * GWT's {@code ComplexPanel} closely enough that widget subclasses can override {@code insert} and
 * call {@code getChildren()}, {@code adopt()} and {@code adjustIndex()} exactly as they do on GWT.
 */
public abstract class ComplexPanel extends Panel implements IndexedPanel.ForIsWidget {

  private final WidgetCollection children = new WidgetCollection(this);

  protected WidgetCollection getChildren() {
    return children;
  }

  @Override
  public void add(final Widget child) {
    add(child, getElement());
  }

  /** Adds a child to the given container element. */
  protected void add(final Widget child, final Element container) {
    child.removeFromParent();
    getChildren().add(child);
    container.appendChild(child.getElement());
    adopt(child);
  }

  @Deprecated
  protected void add(final Widget child, final com.google.gwt.user.client.Element container) {
    add(child, (Element) container);
  }

  protected void insert(
      final Widget child, final Element container, final int beforeIndex, final boolean domInsert) {
    insert(child, com.google.gwt.user.client.Element.as(container), beforeIndex, domInsert);
  }

  @Deprecated
  protected void insert(
      final Widget child,
      final com.google.gwt.user.client.Element container,
      int beforeIndex,
      final boolean domInsert) {
    beforeIndex = adjustIndex(child, beforeIndex);
    child.removeFromParent();
    getChildren().insert(child, beforeIndex);
    if (domInsert) {
      // GWT indexes the container's element children. A plugin may have wrapped
      // a widget element, so the next logical widget need not be a direct child.
      com.google.gwt.user.client.DOM.insertChild(container, child.getElement(), beforeIndex);
    } else {
      container.appendChild(child.getElement());
    }
    adopt(child);
  }

  /**
   * Clamps an insertion index, and compensates for the child already being present earlier in this
   * panel — removing it first would shift everything after it down.
   */
  protected int adjustIndex(final Widget child, int beforeIndex) {
    checkIndexBoundsForInsertion(beforeIndex);
    if (child.getParent() == this) {
      final int idx = getWidgetIndex(child);
      if (idx < beforeIndex) {
        beforeIndex--;
      }
    }
    return beforeIndex;
  }

  protected void checkIndexBoundsForInsertion(final int index) {
    if (index < 0 || index > getWidgetCount()) {
      throw new IndexOutOfBoundsException("index=" + index + ", size=" + getWidgetCount());
    }
  }

  protected void checkIndexBoundsForAccess(final int index) {
    if (index < 0 || index >= getWidgetCount()) {
      throw new IndexOutOfBoundsException("index=" + index + ", size=" + getWidgetCount());
    }
  }

  @Override
  public boolean remove(final Widget child) {
    if (child == null || child.getParent() != this) {
      return false;
    }
    orphan(child);
    child.getElement().removeFromParent();
    getChildren().remove(child);
    return true;
  }

  @Override
  public boolean remove(final int index) {
    return remove(getWidget(index));
  }

  @Override
  public Widget getWidget(final int index) {
    return getChildren().get(index);
  }

  @Override
  public int getWidgetCount() {
    return getChildren().size();
  }

  @Override
  public int getWidgetIndex(final Widget child) {
    return getChildren().indexOf(child);
  }

  @Override
  public int getWidgetIndex(final IsWidget child) {
    return getWidgetIndex(child == null ? null : child.asWidget());
  }

  @Override
  public Iterator<Widget> iterator() {
    return new Iterator<Widget>() {
      private int index = -1;

      @Override
      public boolean hasNext() {
        return index + 1 < getWidgetCount();
      }

      @Override
      public Widget next() {
        if (!hasNext()) {
          throw new java.util.NoSuchElementException();
        }
        return getWidget(++index);
      }

      @Override
      public void remove() {
        if (index < 0) {
          throw new IllegalStateException("next() has not been called");
        }
        ComplexPanel.this.remove(getWidget(index));
        index--;
      }
    };
  }
}
