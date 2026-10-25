package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

// Preserve the public GWT hierarchy: callers rely on assignability to CellPanel and Panel.
@SuppressWarnings("java:S110")
public class VerticalPanel extends CellPanel implements InsertPanel.ForIsWidget {

  @Override
  public void add(Widget child) {
    insert(child, getWidgetCount());
  }

  public void insert(Widget child, int beforeIndex) {
    int index = adjustIndex(child, beforeIndex);
    child.removeFromParent();
    Element row = Document.get().createElement("tr");
    if (index < getWidgetCount())
      body.insertBefore(row, getCellElement(getWidget(index)).getParentElement());
    else body.appendChild(row);
    addCell(child, row, index);
  }

  public void insert(IsWidget child, int beforeIndex) {
    insert(child.asWidget(), beforeIndex);
  }

  @Override
  public boolean remove(Widget child) {
    if (child == null || child.getParent() != this) return false;
    Element row = getCellElement(child).getParentElement();
    try {
      return super.remove(child);
    } finally {
      row.removeFromParent();
    }
  }
}
