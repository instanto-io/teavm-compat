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
package com.google.gwt.user.client.ui.impl;

import com.google.gwt.dom.client.Element;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;

/** Wires a form element's submit and its target frame's load back to the host panel. */
public class FormPanelImpl {

  /** Hooks the form's submit event and the frame's load event to {@code host}. */
  public void hookEvents(final Element iframe, final Element form, final FormPanelImplHost host) {
    if (form != null) {
      form.unwrap()
          .addEventListener(
              "submit",
              (Event event) -> {
                if (!host.onFormSubmit()) {
                  event.preventDefault();
                }
              });
    }
    if (iframe != null) {
      iframe.unwrap().addEventListener("load", (Event event) -> host.onFrameLoad());
    }
  }

  public void unhookEvents(final Element iframe, final Element form) {
    // Listeners are bound to the elements' lifetime; nothing to unhook explicitly.
  }

  public String getContents(final Element iframe) {
    return frameText(iframe.unwrap());
  }

  public String getEncoding(final Element form) {
    return form.getPropertyString("enctype");
  }

  public void setEncoding(final Element form, final String encoding) {
    form.setPropertyString("enctype", encoding);
  }

  public void submit(final Element form, final Element iframe) {
    submitForm(form.unwrap());
  }

  @JSBody(
      params = {"el"},
      script =
          "try { return el.contentDocument ? el.contentDocument.body.innerHTML : null; }"
              + " catch (e) { return null; }")
  private static native String frameText(HTMLElement el);

  @JSBody(
      params = {"el"},
      script = "el.submit();")
  private static native void submitForm(HTMLElement el);

  public void reset(final Element form) {
    resetForm(form.unwrap());
  }

  @JSBody(
      params = {"el"},
      script = "el.reset();")
  private static native void resetForm(HTMLElement el);
}
