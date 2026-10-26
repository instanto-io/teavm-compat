package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import io.instanto.teavm.module.ScriptModule;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.*;
import org.teavm.junit.*;

/** Browser contracts exposed as missing paths by original-bytecode coverage. */
@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class PanelTimerAndModuleContractsTest {
  @Test
  public void absolutePositioningRestoresFlowAndRejectsForeignChildren() {
    AbsolutePanel panel = new AbsolutePanel();
    Label child = new Label("positioned");
    Label foreign = new Label("foreign");
    IsWidget wrapped = () -> child;
    RootPanel.get().add(panel);
    try {
      panel.add(wrapped, 12, 18);
      assertEquals(12, panel.getWidgetLeft(child));
      assertEquals(18, panel.getWidgetTop(child));
      assertEquals("absolute", child.getElement().getStyle().getProperty("position"));
      panel.setWidgetPosition(wrapped, -1, -1);
      assertEquals("", child.getElement().getStyle().getProperty("position"));
      assertEquals("", child.getElement().getStyle().getProperty("left"));
      panel.setWidgetPosition(child, -1, 5);
      assertEquals("absolute", child.getElement().getStyle().getProperty("position"));
      assertEquals(5, panel.getWidgetTop(child));
      assertThrows(IllegalArgumentException.class, () -> panel.setWidgetPosition(foreign, 0, 0));
      assertThrows(IllegalArgumentException.class, () -> panel.getWidgetLeft(foreign));
      assertThrows(IllegalArgumentException.class, () -> panel.getWidgetTop(foreign));
      assertFalse(panel.remove(null));
      assertFalse(panel.remove(foreign));
      assertTrue(panel.remove(child));
      assertNull(child.getParent());
      assertEquals("", child.getElement().getStyle().getProperty("top"));
      assertFalse(panel.remove(child));
    } finally {
      panel.removeFromParent();
    }
  }

  @Test
  public void focusPanelDispatchesRegisteredEventsAndRemovesHandlers() throws Exception {
    Label child = new Label("content");
    FocusPanel panel = new FocusPanel(child);
    List<String> events = new ArrayList<>();
    var focus = panel.addFocusHandler(e -> events.add("focus"));
    panel.addBlurHandler(e -> events.add("blur"));
    panel.addKeyDownHandler(e -> events.add("keydown"));
    panel.addKeyUpHandler(e -> events.add("keyup"));
    panel.addKeyPressHandler(e -> events.add("keypress"));
    panel.addMouseDownHandler(e -> events.add("mousedown"));
    panel.addMouseUpHandler(e -> events.add("mouseup"));
    panel.addMouseMoveHandler(e -> events.add("mousemove"));
    panel.addMouseOverHandler(e -> events.add("mouseover"));
    panel.addMouseOutHandler(e -> events.add("mouseout"));
    panel.addMouseWheelHandler(e -> events.add("wheel"));
    panel.addTouchStartHandler(e -> events.add("touchstart"));
    panel.addTouchMoveHandler(e -> events.add("touchmove"));
    panel.addTouchEndHandler(e -> events.add("touchend"));
    panel.addTouchCancelHandler(e -> events.add("touchcancel"));
    RootPanel.get().add(panel);
    try {
      assertSame(child, panel.getWidget());
      assertEquals(0, panel.getTabIndex());
      panel.setTabIndex(3);
      assertEquals(3, panel.getTabIndex());
      panel.setAccessKey('t');
      assertEquals("t", panel.getElement().getPropertyString("accessKey"));
      // TeaVM runs tests inside an iframe. Activate its document before testing native
      // focus delivery, and await the events instead of assuming activation finished at entry.
      focusTestDocument();
      BrowserWait.until(PanelTimerAndModuleContractsTest::testDocumentHasFocus);
      panel.setFocus(true);
      BrowserWait.until(() -> events.contains("focus"));
      assertTrue("Focus handler must receive the browser focus event", events.contains("focus"));
      assertTrue(
          "setFocus(true) must focus the panel", isActiveElement(panel.getElement().unwrap()));
      panel.setFocus(false);
      BrowserWait.until(() -> events.contains("blur"));
      assertTrue("Blur handler must receive the browser blur event", events.contains("blur"));
      assertFalse(
          "setFocus(false) must blur the panel", isActiveElement(panel.getElement().unwrap()));
      for (String type :
          List.of(
              "keydown",
              "keyup",
              "keypress",
              "mousedown",
              "mouseup",
              "mousemove",
              "mouseover",
              "mouseout",
              "wheel",
              "touchstart",
              "touchmove",
              "touchend",
              "touchcancel")) {
        events.clear();
        dispatch(panel.getElement().unwrap(), type);
        assertEquals(List.of(type), events);
      }
      focus.removeHandler();
      events.clear();
      panel.setFocus(true);
      BrowserWait.nextEventLoopTurn();
      assertTrue(
          "The panel must refocus after handler removal",
          isActiveElement(panel.getElement().unwrap()));
      assertTrue("Removed focus handler must stay detached", events.isEmpty());
    } finally {
      panel.removeFromParent();
    }
  }

  @Test
  public void timerValidationCancellationAndReschedulingPreserveState() {
    Timer timer =
        new Timer() {
          public void run() {
            fail("Canceled timer fired");
          }
        };
    assertFalse(timer.isRunning());
    timer.cancel();
    assertThrows(IllegalArgumentException.class, () -> timer.schedule(-1));
    assertThrows(IllegalArgumentException.class, () -> timer.scheduleRepeating(0));
    assertThrows(IllegalArgumentException.class, () -> timer.scheduleRepeating(-1));
    timer.schedule(10000);
    assertTrue(timer.isRunning());
    timer.scheduleRepeating(10000);
    assertTrue(timer.isRunning());
    timer.schedule(10000);
    timer.cancel();
    timer.cancel();
    assertFalse(timer.isRunning());
  }

  @Test
  public void oneShotStopsBeforeCallbackAndRepeatingTimerCanCancelItself() throws Exception {
    int[] fired = {0};
    Timer oneShot =
        new Timer() {
          public void run() {
            assertFalse(isRunning());
            fired[0]++;
          }
        };
    Timer repeated =
        new Timer() {
          public void run() {
            if (++fired[0] == 3) cancel();
          }
        };
    try {
      oneShot.schedule(0);
      BrowserWait.until(() -> fired[0] == 1);
      repeated.scheduleRepeating(1);
      BrowserWait.until(() -> !repeated.isRunning());
      assertEquals(3, fired[0]);
    } finally {
      oneShot.cancel();
      repeated.cancel();
    }
  }

  @Test
  public void modulePresenceSkipsAssetsAndReadyHandlersRunImmediately() {
    int[] calls = {0};
    ScriptModule existing = ScriptModule.named("present").presence(() -> true);
    existing.whenReady(null);
    existing.ensureLoaded();
    existing.addReadyHandler(() -> calls[0]++).removeHandler();
    assertTrue(existing.isReady());
    ScriptModule module =
        ScriptModule.named("skipped-script").script("must-not-load.js", () -> true);
    module.whenReady(() -> calls[0]++);
    module.ensureLoaded();
    module.onFailure(error -> fail("Ready module failed"));
    module.whenReady(() -> calls[0]++);
    assertTrue(module.isReady());
    assertEquals(3, calls[0]);
  }

  @Test
  public void missingModulePresenceNotifiesAllFailuresAndIgnoresReadyCallbacks() {
    List<Throwable> errors = new ArrayList<>();
    ScriptModule module = ScriptModule.named("missing-api").presence(() -> false);
    module.onFailure(
        error -> {
          throw new AssertionError("observer failure");
        });
    module.onFailure(errors::add);
    UmbrellaException failure =
        assertThrows(
            UmbrellaException.class,
            () -> module.whenReady(() -> fail("Missing API reported ready")));
    assertEquals(1, failure.getCauses().size());
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains("missing-api"));
    assertTrue(errors.get(0).getMessage().contains("(no scripts)"));
    module.onFailure(errors::add);
    assertSame(errors.get(0), errors.get(1));
    module.ensureLoaded();
    module.whenReady(() -> fail("Failed module retried"));
    assertFalse(module.isReady());
  }

  @JSBody(script = "window.focus();")
  private static native void focusTestDocument();

  @JSBody(script = "return document.hasFocus();")
  private static native boolean testDocumentHasFocus();

  @JSBody(params = "element", script = "return document.activeElement === element;")
  private static native boolean isActiveElement(JSObject element);

  @JSBody(
      params = {"element", "type"},
      script = "element.dispatchEvent(new Event(type,{bubbles:true,cancelable:true}));")
  private static native void dispatch(JSObject element, String type);
}
