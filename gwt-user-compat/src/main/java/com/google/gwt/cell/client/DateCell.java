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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import java.util.Date;

/** Renders a date through a {@link DateTimeFormat}. */
public class DateCell extends AbstractCell<Date> {

  private final DateTimeFormat format;
  private final SafeHtmlRenderer<String> renderer;

  public DateCell() {
    this(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM));
  }

  public DateCell(final DateTimeFormat format) {
    this(format, SimpleSafeHtmlRenderer.getInstance());
  }

  public DateCell(final DateTimeFormat format, final SafeHtmlRenderer<String> renderer) {
    this.format = format;
    this.renderer = renderer;
  }

  @Override
  public void render(final Context context, final Date value, final SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(renderer.render(format.format(value)));
    }
  }
}
