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
 * A group of browser assets, and a way to know when they are usable.
 *
 * <p>This is the part GWT's module system does not have. A {@code .gwt.xml} says what to load and
 * what depends on what at compile time, but nothing says when a library is <em>ready</em>:
 * onModuleLoad returns void, so a module cannot report that its own initialisation is still in
 * flight, and nothing can wait for it. GWT once could -- its script element took a JavaScript block
 * that it polled before starting the application -- and the module parser still carries the notice
 * that the feature was withdrawn: "Injected scripts no longer use an associated JavaScript block;
 * ignoring." What replaced it, ScriptInjector, offers no ordering and no completion signal at all.
 *
 * <p>Without one, every widget invents the same workaround: ask whether the library has appeared,
 * and if not, poll on a timer until it does or a timeout gives up quietly. Four widgets here did
 * exactly that, each with its own interval and its own silent deadline. This replaces all of it
 * with the browser's own answer -- a script element reports when it has run, and when it has
 * failed.
 *
 * <p>Three things follow from that, all of which were previously left to each caller: scripts in
 * one module load in the order declared, because each waits for the one before; a module that
 * cannot load says so once, naming itself and the file, rather than timing out into silence; and a
 * library already on the page is recognised without loading anything, so an application that brings
 * its own copy is never made to take ours. That last point is what keeps the module system
 * optional.
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

  private ScriptModule(final String name) {
    this.name = name;
  }

  /** Begins a module. The name appears in diagnostics, so make it the module's own. */
  public static ScriptModule named(final String name) {
    return new ScriptModule(name);
  }

  /** Adds a stylesheet. Order does not matter for these, so they are not chained. */
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
    for (final Runnable action : pending) {
      action.run();
    }
  }

  private void fail(final String src) {
    failed = true;
    waiting.clear();
    report(name, src);
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

  /**
   * Adds a script and reports how it went.
   *
   * <p>An existing tag is reused rather than duplicated, so two modules sharing a dependency load
   * it once and both hear about it. The handlers are attached before the src is set: afterwards is
   * a race a cached file can already have won.
   */
  @JSBody(
      params = {"src", "onLoad", "onError"},
      script =
          "var existing = document.querySelector('script[src=\"' + src + '\"]');"
              + "if (existing) {"
              + "  if (existing.getAttribute('data-loaded') === 'true') { onLoad.run(); return; }"
              + "  existing.addEventListener('load', function () { onLoad.run(); });"
              + "  existing.addEventListener('error', function () { onError.run(); });"
              + "  return;"
              + "}"
              + "var script = document.createElement('script');"
              + "script.addEventListener('load', function () {"
              + "  script.setAttribute('data-loaded', 'true');"
              + "  onLoad.run();"
              + "});"
              + "script.addEventListener('error', function () { onError.run(); });"
              + "script.src = src;"
              + "document.head.appendChild(script);")
  private static native void addScript(String src, JsAction onLoad, JsAction onError);
}
