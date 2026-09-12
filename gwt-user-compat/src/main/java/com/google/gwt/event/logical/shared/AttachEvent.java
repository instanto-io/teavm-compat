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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/** Fired when a widget is attached to, or detached from, the document. */
public class AttachEvent extends GwtEvent<AttachEvent.Handler> {

  /** Receives attach and detach notifications. */
  public interface Handler extends EventHandler {
    void onAttachOrDetach(AttachEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>();

  private final boolean attached;

  protected AttachEvent(final boolean attached) {
    this.attached = attached;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public static void fire(final HasHandlers source, final boolean attached) {
    source.fireEvent(new AttachEvent(attached));
  }

  public boolean isAttached() {
    return attached;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final Handler handler) {
    handler.onAttachOrDetach(this);
  }
}
