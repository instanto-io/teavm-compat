package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.*;

/** Contracts required by drag controllers, exercised against actual browser elements. */
@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class DragDropCompatibilityTest {
  @Test
  public void mouseEventsCarryRelativeCoordinatesAndGwtButtonFlags() {
    AbsolutePanel panel = new AbsolutePanel();
    FocusPanel handle = new FocusPanel();
    RootPanel.get().add(panel);
    panel.add(handle, 31, 47);
    handle.setPixelSize(100, 60);
    boolean[] called = {false};
    handle.addMouseUpHandler(
        event -> {
          called[0] = true;
          assertEquals(13, event.getX());
          assertEquals(17, event.getY());
          assertEquals(NativeEvent.BUTTON_MIDDLE, event.getNativeButton());
          assertEquals(81, event.getScreenX());
          assertEquals(93, event.getScreenY());
          assertTrue(event.isControlKeyDown());
          assertTrue(event.isShiftKeyDown());
          assertFalse(event.isAltKeyDown());
        });
    try {
      NativeEvent event =
          Document.get()
              .createMouseUpEvent(
                  1,
                  81,
                  93,
                  handle.getAbsoluteLeft() - Window.getScrollLeft() + 13,
                  handle.getAbsoluteTop() - Window.getScrollTop() + 17,
                  true,
                  false,
                  true,
                  false,
                  NativeEvent.BUTTON_MIDDLE);
      handle.getElement().dispatchEvent(event);
      assertTrue(called[0]);
      assertEquals(
          NativeEvent.BUTTON_LEFT,
          Document.get()
              .createMouseUpEvent(
                  1, 0, 0, 0, 0, false, false, false, false, NativeEvent.BUTTON_LEFT)
              .getButton());
      assertEquals(
          NativeEvent.BUTTON_RIGHT,
          Document.get()
              .createMouseUpEvent(
                  1, 0, 0, 0, 0, false, false, false, false, NativeEvent.BUTTON_RIGHT)
              .getButton());
    } finally {
      panel.removeFromParent();
    }
  }

  @Test
  public void rootSupportsPositioningAndInsertPanelsExposeTheirIndexes() {
    AbsolutePanel root = RootPanel.get();
    Label item = new Label("drag");
    root.add(item, 12, 24);
    try {
      item.setPixelSize(80, 30);
      assertEquals(12, root.getWidgetLeft(item));
      assertEquals(24, root.getWidgetTop(item));
      assertEquals(80, item.getOffsetWidth());
      item.setPixelSize(-1, -1);
      assertEquals("", item.getElement().getStyle().getProperty("width"));
      InsertPanel panel = new VerticalPanel();
      panel.add(new Label("first"));
      Label second = new Label("second");
      panel.insert(second, 0);
      assertEquals(2, panel.getWidgetCount());
      assertEquals(0, panel.getWidgetIndex(second));
      assertSame(second, panel.getWidget(0));
    } finally {
      item.removeFromParent();
    }
  }

  @Test
  public void domHelpersRespectContainmentAndPositionedAncestors() {
    AbsolutePanel panel = new AbsolutePanel();
    Label child = new Label("child");
    RootPanel.get().add(panel);
    panel.add(child, 5, 7);
    try {
      assertTrue(DOM.isOrHasChild(panel.getElement(), child.getElement()));
      assertFalse(DOM.isOrHasChild(child.getElement(), panel.getElement()));
      assertSame(panel.getElement().unwrap(), child.getElement().getOffsetParent().unwrap());
      assertEquals("DIV", child.getElement().getNodeName());
      child.getElement().getStyle().setPropertyPx("left", 19);
      child.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
      assertEquals("19px", DOM.getStyleAttribute(child.getElement(), "left"));
      assertEquals("solid", DOM.getStyleAttribute(child.getElement(), "borderStyle"));
    } finally {
      panel.removeFromParent();
    }
  }
}
