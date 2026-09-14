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
package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;

/** A panel wrapping a {@code form} element. */
public class FormPanel extends SimplePanel {

  /** Standard form encodings. */
  public static final String ENCODING_MULTIPART = "multipart/form-data";

  public static final String ENCODING_URLENCODED = "application/x-www-form-urlencoded";
  public static final String METHOD_GET = "get";
  public static final String METHOD_POST = "post";

  public FormPanel() {
    this(Document.get().createFormElement());
  }

  public FormPanel(final Element element) {
    super(element);
  }

  /**
   * @param createIFrame whether to create a hidden target frame so the response is not loaded over
   *     the page. The frame is created lazily on submit.
   */
  public FormPanel(final Element element, final boolean createIFrame) {
    this(element);
    this.createIFrame = createIFrame;
  }

  /** Submits into the named frame. */
  public FormPanel(final NamedFrame frameTarget) {
    this();
    setTarget(frameTarget == null ? null : frameTarget.getName());
  }

  /** Submits into the frame or window with this name. */
  public FormPanel(final String target) {
    this();
    setTarget(target);
  }

  private boolean createIFrame = true;

  public String getTarget() {
    return getFormElement().getTarget();
  }

  public void setTarget(final String target) {
    if (target == null || target.isEmpty()) {
      getElement().removeAttribute("target");
    } else {
      getFormElement().setTarget(target);
    }
  }

  protected FormElement getFormElement() {
    return FormElement.as(getElement());
  }

  public String getAction() {
    return getFormElement().getAction();
  }

  public void setAction(final String action) {
    getFormElement().setAction(action);
  }

  public String getMethod() {
    return getFormElement().getMethod();
  }

  public void setMethod(final String method) {
    getFormElement().setMethod(method);
  }

  public String getEncoding() {
    return getElement().getPropertyString("enctype");
  }

  public void setEncoding(final String encoding) {
    getElement().setPropertyString("enctype", encoding);
  }

  public void submit() {
    getFormElement().submit();
  }

  public void reset() {
    resetForm(getElement().unwrap());
  }

  @org.teavm.jso.JSBody(
      params = {"el"},
      script = "el.reset();")
  private static native void resetForm(org.teavm.jso.dom.html.HTMLElement el);
}
