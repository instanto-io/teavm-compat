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
package com.google.gwt.dom.client;

import org.teavm.jso.dom.html.HTMLElement;

/** Typed view of a {@code <Form>}-family element. */
public class FormElement extends Element {

  public FormElement(final HTMLElement element) {
    super(element);
  }

  public static FormElement as(final Element element) {
    return element == null ? null : new FormElement(element.unwrap());
  }

  public String getAction() {
    return getPropertyString("action");
  }

  public void setAction(final String action) {
    setPropertyString("action", action);
  }

  public String getMethod() {
    return getPropertyString("method");
  }

  public void setMethod(final String method) {
    setPropertyString("method", method);
  }

  public String getTarget() {
    return getPropertyString("target");
  }

  public void setTarget(final String target) {
    setPropertyString("target", target);
  }

  public void submit() {
    submitForm(unwrap());
  }

  @org.teavm.jso.JSBody(
      params = {"el"},
      script = "el.submit();")
  private static native void submitForm(org.teavm.jso.dom.html.HTMLElement el);

  public static final String TAG = "form";

  /** True when the element is a {@code <form>}. */
  public static boolean is(final Element element) {
    if (element == null) {
      return false;
    }
    final String tag = element.getTagName();
    return "form".equalsIgnoreCase(tag);
  }

  public void setAction(final com.google.gwt.safehtml.shared.SafeUri url) {
    setAction(url == null ? "" : url.asString());
  }
}
