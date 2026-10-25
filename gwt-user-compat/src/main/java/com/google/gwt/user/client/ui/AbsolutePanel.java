package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

/** Positions children relative to this panel; (-1,-1) restores normal flow. */
public class AbsolutePanel extends ComplexPanel {
  private static final String POSITION = "position";

  public AbsolutePanel() {
    this(Document.get().createDivElement());
  }

  protected AbsolutePanel(Element element) {
    setElement(element);
    element.getStyle().setProperty(POSITION, "relative");
    element.getStyle().setProperty("overflow", "hidden");
  }

  public void add(Widget widget, int left, int top) {
    add(widget);
    setWidgetPosition(widget, left, top);
  }

  public void add(IsWidget widget, int left, int top) {
    add(widget.asWidget(), left, top);
  }

  public void setWidgetPosition(Widget widget, int left, int top) {
    requireChild(widget);
    var style = widget.getElement().getStyle();
    boolean positioned = left != -1 || top != -1;
    style.setProperty(POSITION, positioned ? "absolute" : "");
    style.setProperty("left", positioned ? left + "px" : "");
    style.setProperty("top", positioned ? top + "px" : "");
  }

  public void setWidgetPosition(IsWidget widget, int left, int top) {
    setWidgetPosition(widget.asWidget(), left, top);
  }

  public int getWidgetLeft(Widget widget) {
    requireChild(widget);
    return widget.getElement().getPropertyInt("offsetLeft");
  }

  public int getWidgetTop(Widget widget) {
    requireChild(widget);
    return widget.getElement().getPropertyInt("offsetTop");
  }

  private void requireChild(Widget widget) {
    if (widget.getParent() != this) throw new IllegalArgumentException("Widget is not a child");
  }

  @Override
  public boolean remove(Widget widget) {
    if (widget == null || widget.getParent() != this) return false;
    try {
      return super.remove(widget);
    } finally {
      var style = widget.getElement().getStyle();
      style.setProperty(POSITION, "");
      style.setProperty("left", "");
      style.setProperty("top", "");
    }
  }
}
