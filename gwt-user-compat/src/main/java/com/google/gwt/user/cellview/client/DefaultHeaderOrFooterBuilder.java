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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/** The header or footer a table builds when none is supplied: one cell per column. */
public class DefaultHeaderOrFooterBuilder<T> extends AbstractHeaderOrFooterBuilder<T> {

  public DefaultHeaderOrFooterBuilder(final AbstractCellTable<T> table, final boolean isFooter) {
    super(table, isFooter);
  }

  @Override
  protected boolean buildHeaderOrFooterImpl(final SafeHtmlBuilder sb) {
    final AbstractCellTable<T> table = getTable();
    final String tag = isBuildingFooter() ? "td" : "th";

    boolean any = false;
    for (int i = 0; i < table.getColumnCount(); i++) {
      if ((isBuildingFooter() ? table.getFooter(i) : table.getHeader(i)) != null) {
        any = true;
        break;
      }
    }
    if (!any) {
      return false;
    }

    sb.appendHtmlConstant("<tr>");
    for (int i = 0; i < table.getColumnCount(); i++) {
      final Header<?> header = isBuildingFooter() ? table.getFooter(i) : table.getHeader(i);
      sb.appendHtmlConstant("<" + tag + ">");
      if (header != null) {
        renderHeader(sb, new Cell.Context(0, i, null), castHeader(header));
      }
      sb.appendHtmlConstant("</" + tag + ">");
    }
    sb.appendHtmlConstant("</tr>");
    return true;
  }

  @SuppressWarnings("unchecked")
  private static <H> Header<H> castHeader(final Header<?> header) {
    return (Header<H>) header;
  }
}
