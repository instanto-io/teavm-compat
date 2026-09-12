package com.google.gwt.user.client.ui.impl;

import com.google.gwt.dom.client.Element;

/** Focus operations on the actual focusable element. */
public class FocusImpl {
  private static final FocusImpl INSTANCE = new FocusImpl();

  public static FocusImpl getFocusImplForWidget() {
    return INSTANCE;
  }

  public int getTabIndex(Element element) {
    return element.getPropertyInt("tabIndex");
  }

  public void setTabIndex(Element element, int value) {
    element.setPropertyInt("tabIndex", value);
  }

  public void setAccessKey(Element element, char key) {
    element.setPropertyString("accessKey", String.valueOf(key));
  }

  public void focus(Element element) {
    element.focus();
  }

  public void blur(Element element) {
    element.blur();
  }
}
