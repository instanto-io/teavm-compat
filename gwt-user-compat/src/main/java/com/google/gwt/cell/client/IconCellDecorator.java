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
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import java.util.Set;

/** Wraps another cell, putting an icon beside whatever it renders. */
public class IconCellDecorator<C> implements Cell<C> {

  private final Cell<C> cell;
  private final String iconUrl;

  public IconCellDecorator(final ImageResource icon, final Cell<C> cell) {
    this.cell = cell;
    this.iconUrl = icon == null ? null : icon.getSafeUri();
  }

  public IconCellDecorator(final String iconUrl, final Cell<C> cell) {
    this.cell = cell;
    this.iconUrl = iconUrl;
  }

  @Override
  public boolean dependsOnSelection() {
    return cell.dependsOnSelection();
  }

  @Override
  public Set<String> getConsumedEvents() {
    return cell.getConsumedEvents();
  }

  @Override
  public boolean handlesSelection() {
    return cell.handlesSelection();
  }

  @Override
  public boolean isEditing(final Context context, final Element parent, final C value) {
    return cell.isEditing(context, parent, value);
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final C value,
      final NativeEvent event,
      final ValueUpdater<C> valueUpdater) {
    cell.onBrowserEvent(context, parent, value, event, valueUpdater);
  }

  @Override
  public void render(final Context context, final C value, final SafeHtmlBuilder sb) {
    if (isIconUsed(value) && iconUrl != null) {
      sb.appendHtmlConstant("<img src=\"" + SafeHtmlUtils.htmlEscape(iconUrl) + "\"/>");
    }
    cell.render(context, value, sb);
  }

  @Override
  public boolean resetFocus(final Context context, final Element parent, final C value) {
    return cell.resetFocus(context, parent, value);
  }

  @Override
  public void setValue(final Context context, final Element parent, final C value) {
    final SafeHtmlBuilder sb = new SafeHtmlBuilder();
    render(context, value, sb);
    parent.setInnerSafeHtml(sb.toSafeHtml());
  }

  /** Subclasses override to hide the icon for some values. */
  protected boolean isIconUsed(final C value) {
    return true;
  }

  protected Cell<C> getCell() {
    return cell;
  }
}
