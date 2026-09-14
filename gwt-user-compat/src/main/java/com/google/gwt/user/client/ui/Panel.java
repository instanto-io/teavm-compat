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

import java.util.Iterator;

/** Base class for widgets that contain other widgets. */
public abstract class Panel extends Widget implements HasWidgets {

  @Override
  public abstract void add(Widget widget);

  public void add(final IsWidget widget) {
    add(widget == null ? null : widget.asWidget());
  }

  @Override
  public void clear() {
    final Iterator<Widget> it = iterator();
    while (it.hasNext()) {
      it.next();
      it.remove();
    }
  }

  @Override
  public abstract boolean remove(Widget widget);

  public boolean remove(final IsWidget widget) {
    return widget != null && remove(widget.asWidget());
  }

  /** Takes ownership of a child: sets its parent and attaches it if this panel is attached. */
  protected final void adopt(final Widget child) {
    child.setParent(this);
  }

  @Override
  protected void doAttachChildren() {
    for (final Widget child : this) {
      if (!child.isAttached()) {
        child.onAttach();
      }
    }
  }

  @Override
  protected void doDetachChildren() {
    for (final Widget child : this) {
      if (child.isAttached()) {
        child.onDetach();
      }
    }
  }

  /** Releases a child: detaches it and clears its parent. */
  protected final void orphan(final Widget child) {
    if (child.getParent() == this) {
      child.setParent(null);
    }
  }
}
