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
package com.google.gwt.cell.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders several cells into one, each wrapped in its own span so an event can be attributed to the
 * cell it landed in.
 */
public class CompositeCell<C> extends AbstractCell<C> {

  private final List<HasCell<C, ?>> hasCells;

  public CompositeCell(final List<HasCell<C, ?>> hasCells) {
    super(consumedEventsOf(hasCells));
    this.hasCells = new ArrayList<>(hasCells);
  }

  private static <C> Set<String> consumedEventsOf(final List<HasCell<C, ?>> hasCells) {
    final Set<String> events = new HashSet<>();
    for (final HasCell<C, ?> hasCell : hasCells) {
      final Set<String> consumed = hasCell.getCell().getConsumedEvents();
      if (consumed != null) {
        events.addAll(consumed);
      }
    }
    return events;
  }

  @Override
  public boolean dependsOnSelection() {
    for (final HasCell<C, ?> hasCell : hasCells) {
      if (hasCell.getCell().dependsOnSelection()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean handlesSelection() {
    for (final HasCell<C, ?> hasCell : hasCells) {
      if (hasCell.getCell().handlesSelection()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isEditing(final Context context, final Element parent, final C value) {
    Element child = parent.getFirstChildElement();
    for (final HasCell<C, ?> hasCell : hasCells) {
      if (child != null && isEditing(context, child, value, hasCell)) {
        return true;
      }
      child = child == null ? null : child.getNextSiblingElement();
    }
    return false;
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater) {
    final Element target = Element.as(event.getEventTarget());
    Element child = parent.getFirstChildElement();
    for (final HasCell<C, ?> hasCell : hasCells) {
      if (child != null && target != null && child.isOrHasChild(target)) {
        onBrowserEvent(context, child, value, event, valueUpdater, hasCell);
        return;
      }
      child = child == null ? null : child.getNextSiblingElement();
    }
  }

  @Override
  public void render(final Context context, final C value, final SafeHtmlBuilder sb) {
    for (final HasCell<C, ?> hasCell : hasCells) {
      sb.appendHtmlConstant("<span>");
      render(context, value, sb, hasCell);
      sb.appendHtmlConstant("</span>");
    }
  }

  private <X> boolean isEditing(
      final Context context, final Element child, final C value, final HasCell<C, X> hasCell) {
    return hasCell.getCell().isEditing(context, child, hasCell.getValue(value));
  }

  private <X> void render(
      final Context context, final C value, final SafeHtmlBuilder sb, final HasCell<C, X> hasCell) {
    hasCell.getCell().render(context, hasCell.getValue(value), sb);
  }

  private <X> void onBrowserEvent(
      final Context context,
      final Element child,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater,
      final HasCell<C, X> hasCell) {
    final Cell<X> cell = hasCell.getCell();
    cell.onBrowserEvent(
        context,
        child,
        hasCell.getValue(value),
        event,
        updated -> {
          final FieldUpdater<C, X> fieldUpdater = hasCell.getFieldUpdater();
          if (fieldUpdater != null) {
            fieldUpdater.update(context.getIndex(), value, updated);
          }
          if (valueUpdater != null) {
            valueUpdater.update(value);
          }
        });
  }
}
