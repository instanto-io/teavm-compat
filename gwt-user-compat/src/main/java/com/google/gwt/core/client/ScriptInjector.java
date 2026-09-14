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

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/** Injects script text or URLs into the selected window, with native loading callbacks. */
public final class ScriptInjector {
  /** TeaVM executes in the host window; unlike GWT it has no separate code iframe. */
  public static final JavaScriptObject TOP_WINDOW = JavaScriptObject.of(currentWindow());

  public static final class FromString {
    private final String scriptText;
    private boolean removeTag = true;
    private JavaScriptObject window;

    public FromString(final String scriptText) {
      this.scriptText = scriptText;
    }

    public FromString setWindow(final JavaScriptObject window) {
      this.window = window;
      return this;
    }

    public FromString setRemoveTag(final boolean removeTag) {
      this.removeTag = removeTag;
      return this;
    }

    public JavaScriptObject inject() {
      JSObject target = window == null ? currentWindow() : window.unwrap();
      JSObject script = createScript(target);
      setText(script, scriptText);
      attach(target, script);
      if (removeTag) remove(script);
      return JavaScriptObject.of(script);
    }
  }

  public static final class FromUrl {
    private final String scriptUrl;
    private boolean removeTag;
    private JavaScriptObject window;
    private Callback<Void, Exception> callback;

    private FromUrl(final String scriptUrl) {
      this.scriptUrl = scriptUrl;
    }

    public FromUrl setWindow(final JavaScriptObject window) {
      this.window = window;
      return this;
    }

    public FromUrl setRemoveTag(final boolean removeTag) {
      this.removeTag = removeTag;
      return this;
    }

    public FromUrl setCallback(final Callback<Void, Exception> callback) {
      this.callback = callback;
      return this;
    }

    public JavaScriptObject inject() {
      JSObject target = window == null ? currentWindow() : window.unwrap();
      JSObject script = createScript(target);
      // Capture this injection's settings: the builder can be reused before loading completes.
      Callback<Void, Exception> completion = callback;
      if (completion != null || removeTag) {
        listen(
            script,
            removeTag,
            success -> {
              if (completion != null) {
                if (success) completion.onSuccess(null);
                else completion.onFailure(new CodeDownloadException("onerror() called."));
              }
            });
      }
      setSource(script, scriptUrl);
      attach(target, script);
      return JavaScriptObject.of(script);
    }
  }

  public static FromString fromString(final String scriptText) {
    return new FromString(scriptText);
  }

  public static FromUrl fromUrl(final String scriptUrl) {
    return new FromUrl(scriptUrl);
  }

  private ScriptInjector() {}

  @JSFunctor
  private interface Completion extends JSObject {
    void complete(boolean success);
  }

  @JSBody(script = "return window;")
  private static native JSObject currentWindow();

  @JSBody(
      params = "target",
      script =
          "var doc=target.document; var script=doc.createElement('script'); var source=doc.querySelector('script[nonce]'); if(source) script.setAttribute('nonce', source.nonce || source.getAttribute('nonce')); return script;")
  private static native JSObject createScript(JSObject target);

  @JSBody(
      params = {"script", "text"},
      script = "script.text=text;")
  private static native void setText(JSObject script, String text);

  @JSBody(
      params = {"script", "url"},
      script = "script.src=url;")
  private static native void setSource(JSObject script, String url);

  @JSBody(
      params = {"target", "script"},
      script = "target.document.head.appendChild(script);")
  private static native void attach(JSObject target, JSObject script);

  @JSBody(
      params = "script",
      script = "if(script.parentNode) script.parentNode.removeChild(script);")
  private static native void remove(JSObject script);

  @JSBody(
      params = {"script", "removeTag", "completion"},
      script =
          "var finished=false; function done(success) { if(finished) return; finished=true; script.onload=script.onerror=null; if(removeTag && script.parentNode) script.parentNode.removeChild(script); completion(success); } script.onload=function(){done(true);}; script.onerror=function(){done(false);};")
  private static native void listen(JSObject script, boolean removeTag, Completion completion);
}
