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
package com.google.gwt.core.client;

import org.teavm.jso.JSObject;

/**
 * An opaque handle to a JavaScript value.
 *
 * <p>In GWT this is a JavaScript overlay type: the Java reference <em>is</em> the JavaScript value.
 * TeaVM has no overlay types, so as everywhere else in this compatibility layer the value is
 * wrapped and reached through {@link #unwrap()}. Widget code that only passes one of these between
 * native methods -- which is what the extras do with a noUiSlider, a Quill instance or a date
 * picker -- does not notice the difference.
 */
public class JavaScriptObject {

  private final JSObject value;

  public JavaScriptObject(final JSObject value) {
    this.value = value;
  }

  /** The underlying JavaScript value. */
  public JSObject unwrap() {
    return value;
  }

  /** Wraps a JavaScript value, or null. */
  public static JavaScriptObject of(final JSObject value) {
    return value == null ? null : new JavaScriptObject(value);
  }

  @SuppressWarnings("unchecked")
  public <T extends JavaScriptObject> T cast() {
    return (T) this;
  }
}
