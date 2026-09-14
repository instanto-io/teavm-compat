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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/** Fired as a data-bound view moves between loading and loaded. */
public class LoadingStateChangeEvent extends GwtEvent<LoadingStateChangeEvent.Handler> {

  /** The loading states a view passes through. */
  public interface LoadingState {}

  /** The states a view passes through, as GWT names them. */
  public static final LoadingState LOADING = new DefaultLoadingState("LOADING");

  public static final LoadingState PARTIALLY_LOADED = new DefaultLoadingState("PARTIALLY_LOADED");
  public static final LoadingState LOADED = new DefaultLoadingState("LOADED");

  /** Named loading state. */
  public static final class DefaultLoadingState implements LoadingState {

    private final String name;

    DefaultLoadingState(final String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /** Receives loading-state changes. */
  public interface Handler extends EventHandler {
    void onLoadingStateChanged(LoadingStateChangeEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>();

  private final LoadingState state;

  public LoadingStateChangeEvent(final LoadingState state) {
    this.state = state;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public static void fire(final HasHandlers source, final LoadingState state) {
    source.fireEvent(new LoadingStateChangeEvent(state));
  }

  public LoadingState getLoadingState() {
    return state;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final Handler handler) {
    handler.onLoadingStateChanged(this);
  }
}
