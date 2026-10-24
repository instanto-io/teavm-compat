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

/** A panel positioned above the page, used for dropdowns and suggestion lists. */
public class PopupPanel extends SimplePanel
    implements com.google.gwt.event.logical.shared.HasCloseHandlers<PopupPanel> {
  private com.google.gwt.event.shared.HandlerRegistration previewRegistration;
  private com.google.gwt.event.shared.HandlerRegistration historyRegistration;
  private boolean modal;
  private boolean autoHideOnHistoryEvents = true;
  private final java.util.List<com.google.gwt.dom.client.Element> partners =
      new java.util.ArrayList<>();

  public PopupPanel(boolean autoHide, boolean modal) {
    this(autoHide);
    this.modal = modal;
  }

  public void setModal(boolean modal) {
    this.modal = modal;
  }

  public boolean isModal() {
    return modal;
  }

  public void setAutoHideOnHistoryEventsEnabled(boolean enabled) {
    autoHideOnHistoryEvents = enabled;
  }

  public boolean isAutoHideOnHistoryEventsEnabled() {
    return autoHideOnHistoryEvents;
  }

  public void addAutoHidePartner(com.google.gwt.dom.client.Element element) {
    partners.add(element);
  }

  public void removeAutoHidePartner(com.google.gwt.dom.client.Element element) {
    partners.remove(element);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public com.google.gwt.event.shared.HandlerRegistration addCloseHandler(
      com.google.gwt.event.logical.shared.CloseHandler<PopupPanel> handler) {
    return addHandler(
        handler,
        (com.google.gwt.event.shared.GwtEvent.Type)
            com.google.gwt.event.logical.shared.CloseEvent.getType());
  }

  private void hookPreview() {
    previewRegistration =
        com.google.gwt.user.client.Event.addNativePreviewHandler(
            event -> {
              var target = event.getNativeEvent().getEventTarget();
              boolean inside = target != null && contains(getElement().unwrap(), target.unwrap());
              if (target != null)
                for (var partner : partners) inside |= contains(partner.unwrap(), target.unwrap());
              if (inside) {
                event.consume();
                return;
              }
              String type = event.getNativeEvent().getType();
              if (autoHide && (type.equals("mousedown") || type.equals("touchstart"))) hide(true);
              else if (modal) event.cancel();
            });
    historyRegistration =
        com.google.gwt.user.client.History.addValueChangeHandler(
            event -> {
              if (autoHideOnHistoryEvents) hide(true);
            });
  }

  @org.teavm.jso.JSBody(
      params = {"element", "target"},
      script = "return target && element.contains(target);")
  private static native boolean contains(
      org.teavm.jso.JSObject element, org.teavm.jso.JSObject target);

  @Override
  protected void onDetach() {
    showing = false;
    if (previewRegistration != null) {
      previewRegistration.removeHandler();
      previewRegistration = null;
    }
    if (historyRegistration != null) {
      historyRegistration.removeHandler();
      historyRegistration = null;
    }
    super.onDetach();
  }

  private boolean autoHide;
  private boolean showing;

  public PopupPanel() {
    this(false);
  }

  public PopupPanel(final boolean autoHide) {
    super(Document.get().createDivElement());
    this.autoHide = autoHide;
    getElement().getStyle().setProperty("position", "absolute");
    hide();
  }

  public void setPopupPosition(final int left, final int top) {
    getElement().getStyle().setProperty("left", left + "px");
    getElement().getStyle().setProperty("top", top + "px");
  }

  public void show() {
    if (!showing) {
      RootPanel.get().add(this);
      showing = true;
      hookPreview();
    }
    setVisible(true);
  }

  public void hide() {
    hide(false);
  }

  public void hide(boolean autoClosed) {
    if (!showing) return;
    showing = false;
    try {
      removeFromParent();
    } finally {
      com.google.gwt.event.logical.shared.CloseEvent.fire(this, this, autoClosed);
    }
  }

  public void center() {
    show();
    setPopupPosition(
        Math.max(0, (com.google.gwt.user.client.Window.getClientWidth() - getOffsetWidth()) / 2),
        Math.max(0, (com.google.gwt.user.client.Window.getClientHeight() - getOffsetHeight()) / 2));
  }

  public boolean isShowing() {
    return showing;
  }

  public boolean isAutoHideEnabled() {
    return autoHide;
  }

  public void setAutoHideEnabled(final boolean autoHide) {
    this.autoHide = autoHide;
  }

  public int getOffsetWidth() {
    return getElement().getOffsetWidth();
  }

  public int getOffsetHeight() {
    return getElement().getOffsetHeight();
  }
}
