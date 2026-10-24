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
public class FormPanel extends SimplePanel
    implements com.google.gwt.user.client.ui.impl.FormPanelImplHost {
  public interface SubmitHandler extends com.google.gwt.event.shared.EventHandler {
    void onSubmit(SubmitEvent event);
  }

  public interface SubmitCompleteHandler extends com.google.gwt.event.shared.EventHandler {
    void onSubmitComplete(SubmitCompleteEvent event);
  }

  public static class SubmitEvent extends com.google.gwt.event.shared.GwtEvent<SubmitHandler> {
    private static final Type<SubmitHandler> TYPE = new Type<>();
    private boolean canceled;

    public void cancel() {
      canceled = true;
    }

    public boolean isCanceled() {
      return canceled;
    }

    public Type<SubmitHandler> getAssociatedType() {
      return TYPE;
    }

    protected void dispatch(SubmitHandler handler) {
      handler.onSubmit(this);
    }
  }

  public static class SubmitCompleteEvent
      extends com.google.gwt.event.shared.GwtEvent<SubmitCompleteHandler> {
    private static final Type<SubmitCompleteHandler> TYPE = new Type<>();
    private final String results;

    protected SubmitCompleteEvent(String results) {
      this.results = results;
    }

    public String getResults() {
      return results;
    }

    public Type<SubmitCompleteHandler> getAssociatedType() {
      return TYPE;
    }

    protected void dispatch(SubmitCompleteHandler handler) {
      handler.onSubmitComplete(this);
    }
  }

  private final com.google.gwt.user.client.ui.impl.FormPanelImpl impl =
      new com.google.gwt.user.client.ui.impl.FormPanelImpl();
  private com.google.gwt.dom.client.IFrameElement frame;
  private boolean submitted;

  public com.google.gwt.event.shared.HandlerRegistration addSubmitHandler(SubmitHandler handler) {
    return addHandler(handler, SubmitEvent.TYPE);
  }

  public com.google.gwt.event.shared.HandlerRegistration addSubmitCompleteHandler(
      SubmitCompleteHandler handler) {
    return addHandler(handler, SubmitCompleteEvent.TYPE);
  }

  private void prepare() {
    if (createIFrame && frame == null && getTarget().isEmpty()) {
      frame = Document.get().createIFrameElement();
      frame.setName(Document.get().createUniqueId());
      frame.getStyle().setProperty("display", "none");
      getElement().appendChild(frame);
      getFormElement().setTarget(frame.getName());
    }
    impl.hookEvents(frame, getElement(), this);
  }

  @Override
  protected void onAttach() {
    prepare();
    super.onAttach();
  }

  @Override
  protected void onDetach() {
    try {
      super.onDetach();
    } finally {
      impl.unhookEvents(frame, getElement());
      submitted = false;
      if (frame != null) {
        if (getTarget().equals(frame.getName())) getElement().removeAttribute("target");
        frame.removeFromParent();
        frame = null;
      }
    }
  }

  @Override
  public boolean onFormSubmit() {
    SubmitEvent event = new SubmitEvent();
    fireEvent(event);
    submitted = !event.isCanceled();
    return submitted;
  }

  @Override
  public void onFrameLoad() {
    if (!submitted || frame == null || isInitialDocument(frame.unwrap())) return;
    submitted = false;
    fireEvent(new SubmitCompleteEvent(impl.getContents(frame)));
  }

  @org.teavm.jso.JSBody(
      params = "frame",
      script =
          "try { return frame.contentDocument && frame.contentDocument.URL === 'about:blank'; }"
              + " catch(e) { return false; }")
  private static native boolean isInitialDocument(org.teavm.jso.JSObject frame);

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
    if (frame != null && !frame.getName().equals(target)) {
      impl.unhookEvents(frame, getElement());
      frame.removeFromParent();
      frame = null;
      submitted = false;
    }
    if (target == null || target.isEmpty()) {
      getElement().removeAttribute("target");
    } else {
      getFormElement().setTarget(target);
    }
    if (isAttached()) prepare();
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
    prepare();
    if (onFormSubmit()) impl.submit(getElement(), frame);
  }

  public void reset() {
    resetForm(getElement().unwrap());
  }

  @org.teavm.jso.JSBody(
      params = {"el"},
      script = "el.reset();")
  private static native void resetForm(org.teavm.jso.dom.html.HTMLElement el);
}
