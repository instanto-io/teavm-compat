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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/** A button that performs an action on the row it belongs to. */
public class ActionCell<C> extends AbstractCell<C> {

  /** The action a cell performs when clicked. */
  public interface Delegate<T> {
    void execute(T object);
  }

  private final SafeHtml html;
  private final Delegate<C> delegate;

  public ActionCell(final SafeHtml message, final Delegate<C> delegate) {
    super("click", "keydown");
    this.delegate = delegate;
    this.html =
        SafeHtmlUtils.fromTrustedString(
            "<button type=\"button\" tabindex=\"-1\">" + message.asString() + "</button>");
  }

  public ActionCell(final String text, final Delegate<C> delegate) {
    this(SafeHtmlUtils.fromString(text), delegate);
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if ("click".equals(event.getType()) && isTargetInside(parent, event)) {
      onEnterKeyDown(context, parent, value, event, valueUpdater);
    }
  }

  @Override
  public void render(final Context context, final C value, final SafeHtmlBuilder sb) {
    sb.append(html);
  }

  @Override
  protected void onEnterKeyDown(
      final Context context,
      final Element parent,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater) {
    if (delegate != null) {
      delegate.execute(value);
    }
  }

  /** Guards against clicks that land on the row but outside the button. */
  private static boolean isTargetInside(final Element parent, final NativeEvent event) {
    final Element target = Element.as(event.getEventTarget());
    return target != null && parent.isOrHasChild(target);
  }
}
