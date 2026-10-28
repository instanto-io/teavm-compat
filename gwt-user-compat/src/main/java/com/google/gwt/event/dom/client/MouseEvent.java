package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.Window;

/** Mouse coordinates and button state shared by typed mouse events. */
public abstract class MouseEvent<H extends EventHandler> extends HumanInputEvent<H> {
  public int getClientX() {
    return getNativeEvent().getClientX();
  }

  public int getClientY() {
    return getNativeEvent().getClientY();
  }

  public int getScreenX() {
    return getNativeEvent().getScreenX();
  }

  public int getScreenY() {
    return getNativeEvent().getScreenY();
  }

  public int getNativeButton() {
    return getNativeEvent().getButton();
  }

  public int getRelativeX(Element target) {
    return getClientX()
        + Window.getScrollLeft()
        - target.getAbsoluteLeft()
        + target.getScrollLeft();
  }

  public int getRelativeY(Element target) {
    return getClientY() + Window.getScrollTop() - target.getAbsoluteTop() + target.getScrollTop();
  }

  public int getX() {
    return getRelativeElement() == null ? getClientX() : getRelativeX(getRelativeElement());
  }

  public int getY() {
    return getRelativeElement() == null ? getClientY() : getRelativeY(getRelativeElement());
  }
}
