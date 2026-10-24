package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import java.util.LinkedHashMap;
import java.util.Map;

/** Table cells and their ownership shared by horizontal and vertical panels. */
public abstract class CellPanel extends ComplexPanel
    implements HasHorizontalAlignment, HasVerticalAlignment {
  private final Map<Widget, Element> cells = new LinkedHashMap<>();
  protected final Element body = Document.get().createElement("tbody");
  private HorizontalAlignmentConstant horizontal = ALIGN_LEFT;
  private VerticalAlignmentConstant vertical = ALIGN_TOP;

  protected CellPanel() {
    setElement(Document.get().createTableElement());
    getElement().appendChild(body);
    setSpacing(0);
    setPadding(0);
  }

  protected void addCell(Widget widget, Element row, int index) {
    checkIndexBoundsForInsertion(index);
    widget.removeFromParent();
    Element cell = Document.get().createElement("td");
    row.appendChild(cell);
    cells.put(widget, cell);
    getChildren().insert(widget, index);
    cell.appendChild(widget.getElement());
    setCellHorizontalAlignment(widget, horizontal);
    setCellVerticalAlignment(widget, vertical);
    adopt(widget);
  }

  public Element getCellElement(Widget widget) {
    return cells.get(widget);
  }

  public void setCellWidth(Widget widget, String width) {
    cell(widget).setAttribute("width", width);
  }

  public void setCellHeight(Widget widget, String height) {
    cell(widget).setAttribute("height", height);
  }

  public void setCellHorizontalAlignment(Widget widget, HorizontalAlignmentConstant value) {
    cell(widget).getStyle().setProperty("textAlign", value.getTextAlignString());
  }

  public void setCellVerticalAlignment(Widget widget, VerticalAlignmentConstant value) {
    cell(widget).getStyle().setProperty("verticalAlign", value.getVerticalAlignString());
  }

  private Element cell(Widget widget) {
    Element cell = cells.get(widget);
    if (cell == null) throw new IllegalArgumentException("Widget is not a child");
    return cell;
  }

  @Override
  public boolean remove(Widget widget) {
    if (widget == null || widget.getParent() != this) return false;
    Element cell = cell(widget);
    try {
      return super.remove(widget);
    } finally {
      cells.remove(widget);
      cell.removeFromParent();
    }
  }

  public void setSpacing(int spacing) {
    getElement().setAttribute("cellspacing", Integer.toString(spacing));
  }

  public int getSpacing() {
    return getElement().getPropertyInt("cellSpacing");
  }

  public void setPadding(int padding) {
    getElement().setAttribute("cellpadding", Integer.toString(padding));
  }

  public int getPadding() {
    return getElement().getPropertyInt("cellPadding");
  }

  public HorizontalAlignmentConstant getHorizontalAlignment() {
    return horizontal;
  }

  public void setHorizontalAlignment(HorizontalAlignmentConstant value) {
    horizontal = java.util.Objects.requireNonNull(value);
  }

  public VerticalAlignmentConstant getVerticalAlignment() {
    return vertical;
  }

  public void setVerticalAlignment(VerticalAlignmentConstant value) {
    vertical = java.util.Objects.requireNonNull(value);
  }
}
