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
package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.GwtEvent;

public class CloseEvent<T> extends GwtEvent<CloseHandler<T>> {
  private static final Type<CloseHandler<?>> TYPE = new Type<>();
  private final T target;
  private final boolean autoClosed;

  protected CloseEvent(T target, boolean autoClosed) {
    this.target = target;
    this.autoClosed = autoClosed;
  }

  public static Type<CloseHandler<?>> getType() {
    return TYPE;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public final Type<CloseHandler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  public T getTarget() {
    return target;
  }

  public boolean isAutoClosed() {
    return autoClosed;
  }

  public static <T> void fire(HasCloseHandlers<T> source, T target, boolean autoClosed) {
    source.fireEvent(new CloseEvent<T>(target, autoClosed));
  }

  public static <T> void fire(HasCloseHandlers<T> source, T target) {
    fire(source, target, false);
  }

  protected void dispatch(CloseHandler<T> handler) {
    handler.onClose(this);
  }
}
