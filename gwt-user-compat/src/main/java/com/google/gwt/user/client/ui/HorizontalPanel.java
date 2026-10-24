package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class HorizontalPanel extends CellPanel implements InsertPanel.ForIsWidget {
  private final Element row = Document.get().createElement("tr");

  public HorizontalPanel() {
    body.appendChild(row);
  }

  @Override
  public void add(Widget child) {
    insert(child, getWidgetCount());
  }

  public void insert(Widget child, int beforeIndex) {
    int index = adjustIndex(child, beforeIndex);
    child.removeFromParent();
    Element next = index < getWidgetCount() ? getCellElement(getWidget(index)) : null;
    addCell(child, row, index);
    if (next != null) row.insertBefore(getCellElement(child), next);
  }

  public void insert(IsWidget child, int beforeIndex) {
    insert(child.asWidget(), beforeIndex);
  }
}
