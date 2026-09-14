package com.google.gwt.dom.client;

public class TableSectionElement extends Element {
  public TableSectionElement(org.teavm.jso.dom.html.HTMLElement element) {
    super(element);
  }

  public static TableSectionElement as(Element element) {
    return element == null ? null : new TableSectionElement(element.unwrap());
  }

  public NodeList<TableRowElement> getRows() {
    return new NodeList<>(rows(unwrap()), TableRowElement::new);
  }

  @org.teavm.jso.JSBody(params = "section", script = "return section.rows;")
  private static native org.teavm.jso.JSObject rows(org.teavm.jso.dom.html.HTMLElement section);
}
