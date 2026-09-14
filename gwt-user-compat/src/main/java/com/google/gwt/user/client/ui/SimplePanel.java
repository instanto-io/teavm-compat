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
import java.util.Collections;
import java.util.Iterator;

/** Panel that holds at most one child widget. */
public class SimplePanel extends Panel implements HasOneWidget {

  private Widget widget;

  public SimplePanel() {
    this(Document.get().createDivElement());
  }

  public SimplePanel(final Element element) {
    setElement(element);
  }

  @Override
  public Widget getWidget() {
    return widget;
  }

  @Override
  public void setWidget(final Widget widget) {
    if (widget == this.widget) {
      return;
    }
    if (this.widget != null) {
      remove(this.widget);
    }
    if (widget != null) {
      add(widget);
    }
  }

  @Override
  public void setWidget(final IsWidget widget) {
    setWidget(widget == null ? null : widget.asWidget());
  }

  @Override
  public void add(final Widget child) {
    if (widget != null) {
      throw new IllegalStateException("SimplePanel can only contain one child widget");
    }
    child.removeFromParent();
    getContainerElement().appendChild(child.getElement());
    widget = child;
    adopt(child);
  }

  @Override
  public boolean remove(final Widget child) {
    if (child == null || child != widget) {
      return false;
    }
    orphan(child);
    child.getElement().removeFromParent();
    widget = null;
    return true;
  }

  /** The element children are physically attached to; subclasses may override. */
  protected Element getContainerElement() {
    return getElement();
  }

  @Override
  public Iterator<Widget> iterator() {
    if (widget == null) {
      return Collections.<Widget>emptyList().iterator();
    }
    return new Iterator<Widget>() {
      private boolean consumed;
      private Widget returned;

      @Override
      public boolean hasNext() {
        return !consumed && widget != null;
      }

      @Override
      public Widget next() {
        if (!hasNext()) {
          throw new java.util.NoSuchElementException();
        }
        consumed = true;
        returned = widget;
        return returned;
      }

      @Override
      public void remove() {
        if (returned == null) {
          throw new IllegalStateException("next() has not been called");
        }
        SimplePanel.this.remove(returned);
        returned = null;
      }
    };
  }
}
