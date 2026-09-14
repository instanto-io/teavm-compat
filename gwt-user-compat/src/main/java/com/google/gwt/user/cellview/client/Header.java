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
package com.google.gwt.user.cellview.client;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/** The header or footer of a column. */
public abstract class Header<H> {

  private final Cell<H> cell;
  private ValueUpdater<H> updater;
  private String headerStyleNames;

  public Header(final Cell<H> cell) {
    this.cell = cell;
  }

  public Cell<H> getCell() {
    return cell;
  }

  public abstract H getValue();

  public String getHeaderStyleNames() {
    return headerStyleNames;
  }

  public void setHeaderStyleNames(final String headerStyleNames) {
    this.headerStyleNames = headerStyleNames;
  }

  public void setUpdater(final ValueUpdater<H> updater) {
    this.updater = updater;
  }

  public ValueUpdater<H> getUpdater() {
    return updater;
  }

  public void render(final Cell.Context context, final SafeHtmlBuilder sb) {
    cell.render(context, getValue(), sb);
  }

  public void onBrowserEvent(
      final Cell.Context context, final Element elem, final NativeEvent event) {
    cell.onBrowserEvent(context, elem, getValue(), event, updater);
  }
}
