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

import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;

/** Injects script text or a script URL into the host page. */
public class ScriptInjector {

  public static final Object TOP_WINDOW = new Object();

  /** Builder for injecting inline script text. */
  public static class FromString {

    private final String scriptText;
    private boolean removeTag = true;

    FromString(final String scriptText) {
      this.scriptText = scriptText;
    }

    public FromString setWindow(final Object window) {
      return this;
    }

    public FromString setRemoveTag(final boolean removeTag) {
      this.removeTag = removeTag;
      return this;
    }

    public Object inject() {
      final HTMLDocument document = HTMLDocument.current();
      final HTMLElement script = document.createElement("script");
      script.setAttribute("type", "text/javascript");
      script.setInnerHTML(scriptText);
      document.getHead().appendChild((Node) script);
      if (removeTag) {
        document.getHead().removeChild((Node) script);
      }
      return script;
    }
  }

  /** Builder for injecting an external script URL. */
  public static class FromUrl {

    private final String scriptUrl;

    FromUrl(final String scriptUrl) {
      this.scriptUrl = scriptUrl;
    }

    public FromUrl setWindow(final Object window) {
      return this;
    }

    public Object inject() {
      final HTMLDocument document = HTMLDocument.current();
      final HTMLElement script = document.createElement("script");
      script.setAttribute("type", "text/javascript");
      script.setAttribute("src", scriptUrl);
      document.getHead().appendChild((Node) script);
      return script;
    }
  }

  public static FromString fromString(final String scriptText) {
    return new FromString(scriptText);
  }

  public static FromUrl fromUrl(final String scriptUrl) {
    return new FromUrl(scriptUrl);
  }

  private ScriptInjector() {}
}
