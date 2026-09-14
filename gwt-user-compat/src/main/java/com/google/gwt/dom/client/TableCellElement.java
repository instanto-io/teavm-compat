package com.google.gwt.dom.client;

import org.teavm.jso.dom.html.HTMLElement;

public class TableCellElement extends Element {
  public static final String TAG = "td";

  public TableCellElement(HTMLElement element) {
    super(element);
  }

  public static TableCellElement as(Element element) {
    return element == null ? null : new TableCellElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null
        && (TAG.equalsIgnoreCase(element.getTagName())
            || "th".equalsIgnoreCase(element.getTagName()));
  }
}
