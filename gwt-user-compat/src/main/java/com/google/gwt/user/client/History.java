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
package com.google.gwt.user.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import org.teavm.jso.*;

/** Decoded history tokens with synchronous programmatic events and removable browser listeners. */
public final class History {
  private static final HandlerManager handlers = new HandlerManager(new Object());
  private static String lastToken;
  private static org.teavm.jso.dom.events.Registration registration;

  private History() {}

  @JSBody(
      params = "token",
      script =
          "return encodeURI(token == null ? '' : token).replace(/[?#&;+]/g,function(c){return '%' +"
              + " c.charCodeAt(0).toString(16).toUpperCase();});")
  public static native String encodeHistoryToken(String token);

  @JSBody(
      params = "token",
      script = "try{return decodeURIComponent(token);}catch(e){return token;}")
  private static native String decode(String token);

  @JSBody(script = "return window.location.hash;")
  private static native String hash();

  public static String getToken() {
    String value = hash();
    return value.isEmpty() ? "" : decode(value.substring(1));
  }

  public static void newItem(String token) {
    newItem(token, true);
  }

  public static void newItem(String token, boolean issueEvent) {
    change(token, issueEvent, false);
  }

  public static void replaceItem(String token) {
    replaceItem(token, true);
  }

  public static void replaceItem(String token, boolean issueEvent) {
    change(token, issueEvent, true);
  }

  private static void change(String token, boolean issueEvent, boolean replace) {
    token = token == null ? "" : token;
    if (token.equals(getToken())) return;
    changeHash(encodeHistoryToken(token), replace);
    lastToken = token;
    if (issueEvent) fireCurrentHistoryState();
  }

  @JSBody(
      params = {"token", "replace"},
      script =
          "var url=window.location.pathname+window.location.search+'#'+token;if(replace)window.history.replaceState(null,'',url);else"
              + " window.history.pushState(null,'',url);")
  private static native void changeHash(String token, boolean replace);

  @JSBody(script = "window.history.back();")
  public static native void back();

  @JSBody(script = "window.history.forward();")
  public static native void forward();

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    java.util.Objects.requireNonNull(handler);
    var added =
        handlers.addHandler(
            (com.google.gwt.event.shared.GwtEvent.Type) ValueChangeEvent.getType(), handler);
    if (registration == null) {
      lastToken = getToken();
      registration =
          org.teavm.jso.browser.Window.current()
              .onEvent(
                  "hashchange",
                  event ->
                      com.google.gwt.core.client.Scheduler.runEntry(
                          () -> {
                            String token = getToken();
                            if (!token.equals(lastToken)) {
                              lastToken = token;
                              fireCurrentHistoryState();
                            }
                          }));
    }
    return () -> {
      added.removeHandler();
      if (handlers.getHandlerCount(ValueChangeEvent.getType()) == 0 && registration != null) {
        registration.dispose();
        registration = null;
      }
    };
  }

  public static void fireCurrentHistoryState() {
    handlers.fireEvent(new TokenChange(getToken()));
  }

  private static final class TokenChange extends ValueChangeEvent<String> {
    TokenChange(String token) {
      super(token);
    }
  }
}
