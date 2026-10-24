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
package io.instanto.teavm.module;

import java.util.ArrayList;
import java.util.List;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * Loads a module's scripts in declaration order and reports readiness once. Stylesheets are
 * inserted in declaration order but readiness does not wait for their downloads. Existing scripts
 * owned by the host page must have finished loading before this module starts; supply a presence
 * check to validate their API. ScriptInjector also offers loading callbacks.
 */
public final class ScriptModule {

  /** How a module recognises a library that is already on the page. */
  public interface Presence {
    boolean isPresent();
  }

  /** The shape a JSBody callback can invoke. */
  private interface JsAction extends JSObject {
    void run();
  }

  private final String name;
  private final List<String> stylesheets = new ArrayList<>();
  private final List<String> scripts = new ArrayList<>();
  private final List<Presence> scriptPresence = new ArrayList<>();
  private final List<Runnable> waiting = new ArrayList<>();
  private Presence presence;
  private boolean started;
  private boolean ready;
  private boolean failed;
  private final List<java.util.function.Consumer<Throwable>> failureHandlers = new ArrayList<>();
  private Throwable failure;

  public ScriptModule onFailure(java.util.function.Consumer<Throwable> handler) {
    if (failure != null) handler.accept(failure);
    else if (!ready) failureHandlers.add(handler);
    return this;
  }

  /** Like whenReady, with a removable subscription for widgets detached before loading finishes. */
  public com.google.gwt.event.shared.HandlerRegistration addReadyHandler(Runnable action) {
    whenReady(action);
    return () -> waiting.remove(action);
  }

  private ScriptModule(final String name) {
    this.name = name;
  }

  /** Begins a module. The name appears in diagnostics, so make it the module's own. */
  public static ScriptModule named(final String name) {
    return new ScriptModule(name);
  }

  /** Adds a stylesheet in cascade order; downloads are not chained. */
  public ScriptModule stylesheet(final String href) {
    stylesheets.add(href);
    return this;
  }

  /** Adds a script. These load in the order declared, each after the one before. */
  public ScriptModule script(final String src) {
    return script(src, null);
  }

  /** Adds a script that can be skipped when its dependency is already available. */
  public ScriptModule script(final String src, final Presence present) {
    scripts.add(src);
    scriptPresence.add(present);
    return this;
  }

  /**
   * How to tell the library is already present, for a page that loaded it itself.
   *
   * <p>Optional. Without it a module is ready once its own scripts have run, which is the right
   * answer whenever this module is what put them there.
   */
  public ScriptModule presence(final Presence presence) {
    this.presence = presence;
    return this;
  }

  /** Whether the library is usable now. */
  public boolean isReady() {
    return ready || (presence != null && presence.isPresent());
  }

  /** Loads the module's assets if that has not already started. */
  public void ensureLoaded() {
    if (started || failed || isReady()) {
      return;
    }
    started = true;
    for (final String href : stylesheets) {
      addStylesheet(href);
    }
    loadFrom(0);
  }

  /**
   * Runs an action once the module is usable.
   *
   * <p>Immediately if it already is, which is the ordinary case on a backend that compiles its
   * scripts in, and on any page that brought its own copy.
   */
  public void whenReady(final Runnable action) {
    if (action == null) {
      return;
    }
    if (isReady()) {
      action.run();
      return;
    }
    if (failed) {
      // Reported once already; running the action would fail against a library
      // that is not there, which reads as a bug in the widget rather than a
      // missing file.
      return;
    }
    waiting.add(action);
    ensureLoaded();
  }

  private void loadFrom(final int index) {
    if (index >= scripts.size()) {
      complete();
      return;
    }
    final String src = scripts.get(index);
    final Presence present = scriptPresence.get(index);
    if (present != null && present.isPresent()) {
      loadFrom(index + 1);
      return;
    }
    addScript(src, (JsAction) () -> loadFrom(index + 1), (JsAction) () -> fail(src));
  }

  private void complete() {
    if (presence != null && !presence.isPresent()) {
      // Every file arrived and the library still is not there: the wrong thing was
      // served, which a plain load event cannot tell us on its own.
      fail(scripts.isEmpty() ? "(no scripts)" : scripts.get(scripts.size() - 1));
      return;
    }
    ready = true;
    final List<Runnable> pending = new ArrayList<>(waiting);
    waiting.clear();
    failureHandlers.clear();
    java.util.Set<Throwable> failures = new java.util.LinkedHashSet<>();
    for (final Runnable action : pending) {
      try {
        action.run();
      } catch (Throwable error) {
        failures.add(error);
      }
    }
    if (!failures.isEmpty()) throw new com.google.gwt.event.shared.UmbrellaException(failures);
  }

  private void fail(final String src) {
    failed = true;
    waiting.clear();
    failure = new IllegalStateException("Module " + name + " could not load " + src);
    List<java.util.function.Consumer<Throwable>> handlers = new ArrayList<>(failureHandlers);
    failureHandlers.clear();
    report(name, src);
    java.util.Set<Throwable> failures = new java.util.LinkedHashSet<>();
    for (var handler : handlers) {
      try {
        handler.accept(failure);
      } catch (Throwable error) {
        failures.add(error);
      }
    }
    if (!failures.isEmpty()) throw new com.google.gwt.event.shared.UmbrellaException(failures);
  }

  @JSBody(
      params = {"name", "src"},
      script =
          "if (console && console.error) {"
              + "  console.error('module \"' + name + '\" did not load: ' + src"
              + "    + ' -- widgets from this module will not work');"
              + "}")
  private static native void report(String name, String src);

  @JSBody(
      params = {"href"},
      script =
          "if (!document.querySelector('link[href=\"' + href + '\"]')) {"
              + "  var link = document.createElement('link');"
              + "  link.rel = 'stylesheet';"
              + "  link.href = href;"
              + "  document.head.appendChild(link);"
              + "}")
  private static native void addStylesheet(String href);

  /** Shared per-element load state releases all callbacks after success or failure. */
  @JSBody(
      params = {"src", "onLoad", "onError"},
      script =
          "var url=new URL(src,document.baseURI).href;var all=document.scripts, script=null;for(var"
              + " i=0;i<all.length;i++){if(all[i].src===url){script=all[i];break;}}if(script){var"
              + " state=script.__instantoLoad;if(!state || state.status===1){onLoad.run();return;}"
              + "if(state.status===2){onError.run();return;}"
              + "state.waiters.push({ok:onLoad,fail:onError});return;}"
              + "script=document.createElement('script');var"
              + " state={status:0,waiters:[{ok:onLoad,fail:onError}]};script.__instantoLoad=state;function"
              + " done(ok){if(state.status)return;state.status=ok?1:2;"
              + "script.removeEventListener('load',loaded);script.removeEventListener('error',failed);var"
              + " waiters=state.waiters;state.waiters=[];var error=null;for(var"
              + " i=0;i<waiters.length;i++){try{(ok?waiters[i].ok:waiters[i].fail).run();}catch(e){if(!error)error=e;}}if(error)throw"
              + " error;}function loaded(){done(true);}function failed(){done(false);}"
              + "script.addEventListener('load',loaded);script.addEventListener('error',failed);var"
              + " nonce=document.querySelector('script[nonce]');if(nonce)script.nonce=nonce.nonce;"
              + "script.src=src;document.head.appendChild(script);")
  private static native void addScript(String src, JsAction onLoad, JsAction onError);
}
