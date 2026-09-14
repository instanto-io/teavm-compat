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
package com.google.gwt.view.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Selection model that holds any number of selected values. */
public class MultiSelectionModel<T> extends AbstractSelectionModel<T> {

  private final Map<Object, T> selected = new LinkedHashMap<>();

  public MultiSelectionModel() {
    this(null);
  }

  public MultiSelectionModel(final ProvidesKey<T> keyProvider) {
    super(keyProvider);
  }

  public List<T> getSelectedSet() {
    return new ArrayList<>(selected.values());
  }

  @Override
  public boolean isSelected(final T object) {
    return object != null && selected.containsKey(getKey(object));
  }

  @Override
  public void setSelected(final T object, final boolean select) {
    if (object == null) {
      return;
    }
    final Object key = getKey(object);
    final boolean changed =
        select ? selected.put(key, object) == null : selected.remove(key) != null;
    if (changed) {
      fireSelectionChangeEvent();
    }
  }

  public void clear() {
    if (!selected.isEmpty()) {
      selected.clear();
      fireSelectionChangeEvent();
    }
  }
}
