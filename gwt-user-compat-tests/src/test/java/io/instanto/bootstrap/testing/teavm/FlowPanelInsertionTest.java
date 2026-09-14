package io.instanto.bootstrap.testing.teavm;

import static org.junit.Assert.*;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class FlowPanelInsertionTest {
  @Test
  public void rejectsNullWithoutChangingChildren() {
    FlowPanel panel = new FlowPanel();
    Label existing = new Label("existing");
    panel.add(existing);
    try {
      panel.insert((IsWidget) null, 0);
      fail("Null children must be rejected");
    } catch (NullPointerException expected) {
      assertEquals(1, panel.getWidgetCount());
      assertSame(existing, panel.getWidget(0));
    }
  }

  @Test
  public void insertsWrappedWidgetAtRequestedPosition() {
    FlowPanel panel = new FlowPanel();
    Label existing = new Label("existing");
    Label inserted = new Label("inserted");
    panel.add(existing);
    panel.insert((IsWidget) () -> inserted, 0);
    assertEquals(2, panel.getWidgetCount());
    assertSame(inserted, panel.getWidget(0));
    assertSame(existing, panel.getWidget(1));
  }
}
