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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Ordered collection of a panel's logical children. */
public class WidgetCollection implements Iterable<Widget> {

  private final List<Widget> widgets = new ArrayList<>();
  private final HasWidgets parent;

  public WidgetCollection(final HasWidgets parent) {
    this.parent = parent;
  }

  public void add(final Widget widget) {
    insert(widget, size());
  }

  public void insert(final Widget widget, final int beforeIndex) {
    if (beforeIndex < 0 || beforeIndex > size()) {
      throw new IndexOutOfBoundsException("beforeIndex=" + beforeIndex + ", size=" + size());
    }
    widgets.add(beforeIndex, widget);
  }

  public boolean contains(final Widget widget) {
    return widgets.contains(widget);
  }

  public int indexOf(final Widget widget) {
    return widgets.indexOf(widget);
  }

  public Widget get(final int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException("index=" + index + ", size=" + size());
    }
    return widgets.get(index);
  }

  public void remove(final Widget widget) {
    final int index = indexOf(widget);
    if (index == -1) {
      throw new NoSuchElementException();
    }
    remove(index);
  }

  public void remove(final int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException("index=" + index + ", size=" + size());
    }
    widgets.remove(index);
  }

  public int size() {
    return widgets.size();
  }

  @Override
  public Iterator<Widget> iterator() {
    return widgets.iterator();
  }
}
