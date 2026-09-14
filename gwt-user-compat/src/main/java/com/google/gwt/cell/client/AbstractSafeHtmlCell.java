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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import java.util.Set;

/** Cell that renders its value through a {@link SafeHtmlRenderer}. */
public abstract class AbstractSafeHtmlCell<C> extends AbstractCell<C> {

  private final SafeHtmlRenderer<C> renderer;

  public AbstractSafeHtmlCell(final SafeHtmlRenderer<C> renderer, final String... consumedEvents) {
    super(consumedEvents);
    if (renderer == null) {
      throw new IllegalArgumentException("renderer == null");
    }
    this.renderer = renderer;
  }

  public AbstractSafeHtmlCell(
      final SafeHtmlRenderer<C> renderer, final Set<String> consumedEvents) {
    super(consumedEvents);
    if (renderer == null) {
      throw new IllegalArgumentException("renderer == null");
    }
    this.renderer = renderer;
  }

  public SafeHtmlRenderer<C> getRenderer() {
    return renderer;
  }

  @Override
  public void render(final Context context, final C data, final SafeHtmlBuilder sb) {
    render(context, data == null ? null : renderer.render(data), sb);
  }

  protected abstract void render(Context context, SafeHtml data, SafeHtmlBuilder sb);
}
