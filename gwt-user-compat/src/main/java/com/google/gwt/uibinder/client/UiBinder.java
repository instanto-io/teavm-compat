/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
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
package com.google.gwt.uibinder.client;

/**
 * Binds a {@code .ui.xml} template to an owner type, exactly as GWT's interface does.
 *
 * <p>GWT satisfies {@code GWT.create(SomeBinder.class)} with a compile-time generator. TeaVM has no
 * generator SPI, so the equivalent runs earlier: a build step reads the template and writes an
 * ordinary Java implementation, which is registered under this interface so {@code GWT.create}
 * finds it. The generated code is the same shape GWT's generator emits, so a template written for
 * one backend works on the other.
 *
 * @param <U> the root widget the template produces
 * @param <O> the owner whose {@code @UiField}s and {@code @UiHandler}s are wired
 */
public interface UiBinder<U, O> {

  /** Builds the widget tree and wires {@code owner}. */
  U createAndBindUi(O owner);
}
