package com.google.gwt.dom.client;

import org.teavm.jso.dom.html.HTMLElement;

public class TableRowElement extends Element {
  public static final String TAG = "tr";

  public TableRowElement(HTMLElement element) {
    super(element);
  }

  public static TableRowElement as(Element element) {
    return element == null ? null : new TableRowElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null && (TAG.equalsIgnoreCase(element.getTagName()));
  }

  public NodeList<TableCellElement> getCells() {
    return new NodeList<>(cells(unwrap()), TableCellElement::new);
  }

  public int getSectionRowIndex() {
    return getPropertyInt("sectionRowIndex");
  }

  public int getRowIndex() {
    return getPropertyInt("rowIndex");
  }

  @org.teavm.jso.JSBody(params = "row", script = "return row.cells;")
  private static native org.teavm.jso.JSObject cells(HTMLElement row);
}
