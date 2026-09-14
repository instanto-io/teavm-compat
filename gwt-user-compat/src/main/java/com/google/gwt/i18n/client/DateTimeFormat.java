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
package com.google.gwt.i18n.client;

/** Client entry point for the shared date formatter. */
public class DateTimeFormat extends com.google.gwt.i18n.shared.DateTimeFormat {
  private static final java.util.Map<String, DateTimeFormat> CACHE = new java.util.HashMap<>();

  protected DateTimeFormat(String pattern) {
    super(pattern);
  }

  public static DateTimeFormat getFormat(String pattern) {
    return CACHE.computeIfAbsent(pattern, DateTimeFormat::new);
  }

  public static DateTimeFormat getFormat(PredefinedFormat predefined) {
    return getFormat(predefined.getPattern());
  }
}
