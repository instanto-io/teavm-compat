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
package com.google.gwt.editor.client.adapters;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HasValue;

/** Adapts a {@link HasValue} widget to the editor framework. */
public class TakesValueEditor<T> implements LeafValueEditor<T> {

  private final HasValue<T> peer;

  protected TakesValueEditor(final HasValue<T> peer) {
    this.peer = peer;
  }

  public static <T> TakesValueEditor<T> of(final HasValue<T> peer) {
    return new TakesValueEditor<>(peer);
  }

  @Override
  public T getValue() {
    return peer.getValue();
  }

  @Override
  public void setValue(final T value) {
    peer.setValue(value);
  }
}
