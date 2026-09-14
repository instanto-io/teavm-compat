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
package com.google.gwt.core.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Deferred-binding entry points.
 *
 * <p>GWT resolves {@code GWT.create()} with a compile-time generator chosen per permutation. TeaVM
 * emits a single artifact, so there is nothing to defer; an implementation is found in one of two
 * ways:
 *
 * <ol>
 *   <li>an explicit {@link #register} call, which wins if present; or
 *   <li>a {@link ServiceLoader} lookup, so a library can declare its own default in {@code
 *       META-INF/services/&lt;interface&gt;} and callers need do nothing.
 * </ol>
 *
 * <p>The service lookup is what keeps {@code GWT.create} out of application code: the widget
 * library ships defaults for the types it asks for, so a TeaVM consumer never calls {@code
 * register} unless they are porting a binding of their own.
 */
public final class GWT {

  private static final Map<String, Object> REGISTRY = new HashMap<>();
  private static String moduleBaseUrl = "./";

  private GWT() {}

  /** Registers the implementation {@code create()} should return for {@code type}. */
  public static <T> void register(final Class<T> type, final T implementation) {
    REGISTRY.put(type.getName(), implementation);
  }

  @SuppressWarnings("unchecked")
  public static <T> T create(final Class<?> classLiteral) {
    final Object registered = REGISTRY.get(classLiteral.getName());
    if (registered != null) {
      return (T) registered;
    }
    final Object provided = fromServiceLoader(classLiteral);
    if (provided != null) {
      REGISTRY.put(classLiteral.getName(), provided);
      return (T) provided;
    }
    throw new IllegalStateException(
        "No implementation available for GWT.create("
            + classLiteral.getName()
            + "). Declare one in "
            + "META-INF/services/"
            + classLiteral.getName()
            + ", or call GWT.register("
            + classLiteral.getSimpleName()
            + ".class, impl).");
  }

  private static Object fromServiceLoader(final Class<?> classLiteral) {
    try {
      final Iterator<?> providers = ServiceLoader.load(classLiteral).iterator();
      return providers.hasNext() ? providers.next() : null;
    } catch (final RuntimeException noProvider) {
      return null;
    }
  }

  public static boolean isClient() {
    return true;
  }

  public static boolean isProdMode() {
    return true;
  }

  public static boolean isScript() {
    return true;
  }

  public static String getModuleName() {
    return "teavm";
  }

  public static String getModuleBaseURL() {
    return moduleBaseUrl;
  }

  /**
   * Supplies the deployment location that a GWT compiler normally writes into the generated
   * bootstrap script. TeaVM applications that execute shared GWT source should set this before
   * invoking their entry point.
   */
  public static void setModuleBaseURL(final String value) {
    if (value == null || value.isEmpty()) {
      moduleBaseUrl = "./";
      return;
    }
    moduleBaseUrl = value.endsWith("/") ? value : value + "/";
  }

  public static void log(final String message) {
    System.out.println(message);
  }

  public static void log(final String message, final Throwable t) {
    System.out.println(message);
    if (t != null) {
      t.printStackTrace();
    }
  }
}
