package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

/** Typed value input backed by the supplied renderer and parser. */
public class ValueBox<T> extends ValueBoxBase<T> {
  protected ValueBox(Element element, Renderer<T> renderer, Parser<T> parser) {
    super(element, renderer, parser);
  }

  public int getMaxLength() {
    return getElement().getPropertyInt("maxLength");
  }

  public void setMaxLength(int length) {
    getElement().setPropertyInt("maxLength", length);
  }

  public int getVisibleLength() {
    return getElement().getPropertyInt("size");
  }

  public void setVisibleLength(int length) {
    getElement().setPropertyInt("size", length);
  }
}
