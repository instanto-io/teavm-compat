package com.google.gwt.user.client.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;

/** A focusable single-child panel backed by a div. */
public class FocusPanel extends SimplePanel
    implements Focusable,
        HasBlurHandlers,
        HasFocusHandlers,
        HasKeyDownHandlers,
        HasKeyUpHandlers,
        HasKeyPressHandlers,
        HasMouseDownHandlers,
        HasMouseUpHandlers,
        HasMouseMoveHandlers,
        HasMouseOverHandlers,
        HasMouseOutHandlers,
        HasMouseWheelHandlers,
        HasTouchStartHandlers,
        HasTouchMoveHandlers,
        HasTouchEndHandlers,
        HasTouchCancelHandlers {
  public FocusPanel() {
    getElement().setTabIndex(0);
  }

  public FocusPanel(Widget child) {
    this();
    setWidget(child);
  }

  @Override
  public int getTabIndex() {
    return getElement().getPropertyInt("tabIndex");
  }

  @Override
  public void setTabIndex(int index) {
    getElement().setTabIndex(index);
  }

  public void setAccessKey(char key) {
    getElement().setAccessKey(String.valueOf(key));
  }

  public void setFocus(boolean focus) {
    if (focus) getElement().focus();
    else getElement().blur();
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
    return addDomHandler(handler, KeyUpEvent.getType());
  }

  public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
    return addDomHandler(handler, KeyPressEvent.getType());
  }

  public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
    return addDomHandler(handler, MouseDownEvent.getType());
  }

  public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
    return addDomHandler(handler, MouseUpEvent.getType());
  }

  public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
    return addDomHandler(handler, MouseMoveEvent.getType());
  }

  public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
    return addDomHandler(handler, MouseOverEvent.getType());
  }

  public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
    return addDomHandler(handler, MouseOutEvent.getType());
  }

  public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
    return addDomHandler(handler, MouseWheelEvent.getType());
  }

  public HandlerRegistration addTouchStartHandler(TouchStartHandler handler) {
    return addDomHandler(handler, TouchStartEvent.getType());
  }

  public HandlerRegistration addTouchMoveHandler(TouchMoveHandler handler) {
    return addDomHandler(handler, TouchMoveEvent.getType());
  }

  public HandlerRegistration addTouchEndHandler(TouchEndHandler handler) {
    return addDomHandler(handler, TouchEndEvent.getType());
  }

  public HandlerRegistration addTouchCancelHandler(TouchCancelHandler handler) {
    return addDomHandler(handler, TouchCancelEvent.getType());
  }
}
