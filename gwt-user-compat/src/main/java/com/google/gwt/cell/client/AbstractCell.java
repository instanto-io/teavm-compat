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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Base cell: declares the events it consumes and renders on {@code setValue}. */
public abstract class AbstractCell<C> implements Cell<C> {

  private final Set<String> consumedEvents;

  public AbstractCell(final String... consumedEvents) {
    Set<String> events = null;
    if (consumedEvents != null && consumedEvents.length > 0) {
      events = new HashSet<>();
      Collections.addAll(events, consumedEvents);
    }
    this.consumedEvents = events == null ? null : Collections.unmodifiableSet(events);
  }

  public AbstractCell(final Set<String> consumedEvents) {
    this.consumedEvents =
        consumedEvents == null ? null : Collections.unmodifiableSet(new HashSet<>(consumedEvents));
  }

  @Override
  public boolean dependsOnSelection() {
    return false;
  }

  @Override
  public Set<String> getConsumedEvents() {
    return consumedEvents;
  }

  @Override
  public boolean handlesSelection() {
    return false;
  }

  @Override
  public boolean isEditing(final Context context, final Element parent, final C value) {
    return false;
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater) {
    final String eventType = event.getType();
    if (consumedEvents != null && consumedEvents.contains(eventType)) {
      onEnterKeyDown(context, parent, value, event, valueUpdater);
    }
  }

  /** Called for a consumed event; subclasses override to act on it. */
  protected void onEnterKeyDown(
      final Context context,
      final Element parent,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater) {}

  @Override
  public abstract void render(Context context, C value, SafeHtmlBuilder sb);

  @Override
  public boolean resetFocus(final Context context, final Element parent, final C value) {
    return false;
  }

  @Override
  public void setValue(final Context context, final Element parent, final C value) {
    final SafeHtmlBuilder sb = new SafeHtmlBuilder();
    render(context, value, sb);
    parent.setInnerSafeHtml(sb.toSafeHtml());
  }
}
