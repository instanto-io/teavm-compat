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
package com.google.web.bindery.event.shared;

/** Root event type shared by the GWT and bindery event APIs. */
public abstract class Event<H> {

  /** Identifies a family of events and the handler interface that receives them. */
  public static class Type<H> {}

  private Object source;

  public abstract Type<H> getAssociatedType();

  protected abstract void dispatch(H handler);

  public Object getSource() {
    return source;
  }

  protected void setSource(final Object source) {
    this.source = source;
  }

  /** Lets an event bus in this package dispatch without widening {@code dispatch}. */
  @SuppressWarnings("unchecked")
  final void dispatchUnchecked(final Object handler) {
    dispatch((H) handler);
  }
}
