package com.google.gwt.canvas.client;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.FocusWidget;

/** Canvas widget using the browser's 2D canvas implementation. */
public class Canvas extends FocusWidget {
  protected Canvas() {
    super(Document.get().createCanvasElement());
  }

  public static Canvas createIfSupported() {
    Canvas canvas = new Canvas();
    return canvas.getContext2d() == null ? null : canvas;
  }

  public CanvasElement getCanvasElement() {
    return CanvasElement.as(getElement());
  }

  public Context2d getContext2d() {
    return getCanvasElement().getContext2d();
  }

  public int getCoordinateSpaceWidth() {
    return getCanvasElement().getWidth();
  }

  public int getCoordinateSpaceHeight() {
    return getCanvasElement().getHeight();
  }

  public void setCoordinateSpaceWidth(int value) {
    getCanvasElement().setWidth(value);
  }

  public void setCoordinateSpaceHeight(int value) {
    getCanvasElement().setHeight(value);
  }

  public String toDataUrl() {
    return getCanvasElement().toDataUrl();
  }

  public String toDataUrl(String type) {
    return getCanvasElement().toDataUrl(type);
  }
}
