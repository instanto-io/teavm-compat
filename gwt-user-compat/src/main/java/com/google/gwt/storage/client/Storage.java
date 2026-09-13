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
package com.google.gwt.storage.client;

import org.teavm.jso.JSBody;

/**
 * Web Storage.
 *
 * <p>Every accessor is guarded: a private window, blocked site data or a context with no storage at
 * all throws on access in some browsers, and a widget library should degrade rather than take the
 * page down. {@link #getLocalStorageIfSupported()} returns null in those cases, which is the check
 * callers already make.
 */
public final class Storage {

  private static final Storage LOCAL = new Storage(true);
  private static final Storage SESSION = new Storage(false);

  private final boolean local;

  private Storage(final boolean local) {
    this.local = local;
  }

  public static boolean isLocalStorageSupported() {
    return isSupported(true);
  }

  public static boolean isSessionStorageSupported() {
    return isSupported(false);
  }

  public static Storage getLocalStorageIfSupported() {
    return isSupported(true) ? LOCAL : null;
  }

  public static Storage getSessionStorageIfSupported() {
    return isSupported(false) ? SESSION : null;
  }

  public String getItem(final String key) {
    return get(local, key);
  }

  public void setItem(final String key, final String value) {
    set(local, key, value);
  }

  public void removeItem(final String key) {
    remove(local, key);
  }

  public void clear() {
    clearAll(local);
  }

  public int getLength() {
    return length(local);
  }

  public String key(final int index) {
    return keyAt(local, index);
  }

  @JSBody(
      params = {"local"},
      script =
          "try { var s = local ? window.localStorage : window.sessionStorage;"
              + " return !!s; } catch (e) { return false; }")
  private static native boolean isSupported(boolean local);

  @JSBody(
      params = {"local", "key"},
      script =
          "try { return (local ? window.localStorage : window.sessionStorage).getItem(key); }"
              + " catch (e) { return null; }")
  private static native String get(boolean local, String key);

  @JSBody(
      params = {"local", "key", "value"},
      script =
          "try { (local ? window.localStorage : window.sessionStorage).setItem(key, value); }"
              + " catch (e) { }")
  private static native void set(boolean local, String key, String value);

  @JSBody(
      params = {"local", "key"},
      script =
          "try { (local ? window.localStorage : window.sessionStorage).removeItem(key); }"
              + " catch (e) { }")
  private static native void remove(boolean local, String key);

  @JSBody(
      params = {"local"},
      script =
          "try { (local ? window.localStorage : window.sessionStorage).clear(); } catch (e) { }")
  private static native void clearAll(boolean local);

  @JSBody(
      params = {"local"},
      script =
          "try { return (local ? window.localStorage : window.sessionStorage).length | 0; }"
              + " catch (e) { return 0; }")
  private static native int length(boolean local);

  @JSBody(
      params = {"local", "index"},
      script =
          "try { return (local ? window.localStorage : window.sessionStorage).key(index); }"
              + " catch (e) { return null; }")
  private static native String keyAt(boolean local, int index);
}
