package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.event.dom.client.HasLoadHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeUri;

/** Browser-backed frame using the same URL and load-event contracts as GWT. */
public class Frame extends Widget implements HasLoadHandlers {
  public Frame() {
    setElement(Document.get().createIFrameElement());
    setStyleName("gwt-Frame");
  }

  public Frame(String url) {
    this();
    setUrl(url);
  }

  public Frame(SafeUri url) {
    this(url.asString());
  }

  public String getUrl() {
    return IFrameElement.as(getElement()).getSrc();
  }

  public void setUrl(String url) {
    IFrameElement.as(getElement()).setSrc(url);
  }

  public void setUrl(SafeUri url) {
    setUrl(url.asString());
  }

  @Override
  public HandlerRegistration addLoadHandler(LoadHandler handler) {
    return addDomHandler(handler, LoadEvent.getType());
  }
}
