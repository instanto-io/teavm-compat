package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.animation.client.*;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.*;
import org.teavm.junit.*;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class SchedulingAndPanelTest {
  @Test
  public void finallyRunsAfterNestedEntryAndBeforeDeferred() throws Exception {
    List<String> events = new ArrayList<>();
    Scheduler scheduler = Scheduler.get();
    scheduler.scheduleEntry((Scheduler.ScheduledCommand) () -> events.add("entry"));
    Scheduler.runEntry(
        () -> {
          events.add("start");
          scheduler.scheduleFinally(
              (Scheduler.ScheduledCommand)
                  () -> {
                    events.add("finally");
                    scheduler.scheduleFinally(
                        (Scheduler.ScheduledCommand) () -> events.add("nested-finally"));
                  });
          scheduler.scheduleDeferred(() -> events.add("deferred"));
          Scheduler.runEntry(() -> events.add("nested-entry"));
          events.add("end");
        });
    assertEquals(
        List.of("entry", "start", "nested-entry", "end", "finally", "nested-finally"), events);
    Thread.sleep(80);
    assertEquals("deferred", events.get(events.size() - 1));
  }

  @Test
  public void repeatingFinallyWaitsForNextEntryAndIncrementalStops() throws Exception {
    int[] repeats = {0}, incremental = {0};
    Scheduler.runEntry(
        () -> Scheduler.get().scheduleFinally((Scheduler.RepeatingCommand) () -> ++repeats[0] < 2));
    assertEquals(1, repeats[0]);
    Scheduler.runEntry(() -> {});
    assertEquals(2, repeats[0]);
    Scheduler.get().scheduleIncremental(() -> ++incremental[0] < 250);
    Thread.sleep(150);
    assertEquals(250, incremental[0]);
  }

  @Test
  public void cancelAnimationReleasesPendingFrameAndCompleteRunsOnce() {
    class Frames extends AnimationScheduler {
      AnimationCallback pending;
      int canceled;

      public AnimationHandle requestAnimationFrame(AnimationCallback callback, Element element) {
        pending = callback;
        return new AnimationHandle() {
          public void cancel() {
            pending = null;
            canceled++;
          }
        };
      }

      void tick(double now) {
        AnimationCallback callback = pending;
        pending = null;
        callback.execute(now);
      }
    }
    Frames frames = new Frames();
    List<Double> updates = new ArrayList<>();
    Animation animation =
        new Animation(frames) {
          protected void onUpdate(double progress) {
            updates.add(progress);
          }
        };
    double start = System.currentTimeMillis() + 10000;
    animation.run(100, start);
    assertNotNull(frames.pending);
    animation.cancel();
    assertNull(frames.pending);
    assertFalse(animation.isRunning());
    assertEquals(1, frames.canceled);
    animation.run(100, start);
    frames.tick(start + 50);
    assertEquals(0.5, updates.get(updates.size() - 1), 0.00001);
    frames.tick(start + 100);
    assertEquals(1.0, updates.get(updates.size() - 1), 0);
    assertFalse(animation.isRunning());
    assertNull(frames.pending);
  }

  @Test
  public void cancelingStartedAnimationUsesInterpolatedCompletion() {
    List<Double> values = new ArrayList<>();
    Animation animation =
        new Animation() {
          protected double interpolate(double progress) {
            return progress * 2;
          }

          protected void onUpdate(double progress) {
            values.add(progress);
          }
        };
    animation.run(10000);
    animation.cancel();
    assertFalse(animation.isRunning());
    assertEquals(2.0, values.get(values.size() - 1), 0);
  }

  @Test
  public void tablePanelsMoveAndRemoveTheirOwnedCells() {
    HorizontalPanel horizontal = new HorizontalPanel();
    VerticalPanel vertical = new VerticalPanel();
    Label a = new Label("a"), b = new Label("b");
    horizontal.add(a);
    horizontal.add(b);
    horizontal.insert(b, 0);
    assertSame(b, horizontal.getWidget(0));
    assertEquals(2, horizontal.getWidgetCount());
    vertical.add(b);
    assertEquals(1, horizontal.getWidgetCount());
    assertSame(vertical, b.getParent());
    vertical.remove(b);
    assertNull(b.getParent());
    assertEquals(0, vertical.getElement().getElementsByTagName("tr").getLength());
    horizontal.clear();
    assertEquals(0, horizontal.getElement().getElementsByTagName("td").getLength());
  }

  @Test
  public void previewCancelsDispatchAndCaptureIsReleasedOnDetach() {
    FocusPanel captured = new FocusPanel();
    Label other = new Label("target");
    RootPanel.get().add(captured);
    RootPanel.get().add(other);
    int[] hits = {0};
    captured.addMouseDownHandler(e -> hits[0]++);
    var preview = Event.addNativePreviewHandler(e -> e.cancel());
    try {
      DOM.setCapture(captured.getElement());
      mouseDown(other.getElement().unwrap());
      assertEquals(0, hits[0]);
      preview.removeHandler();
      mouseDown(other.getElement().unwrap());
      assertEquals(1, hits[0]);
      captured.removeFromParent();
      assertNull(DOM.getCaptureElement());
      mouseDown(other.getElement().unwrap());
      assertEquals(1, hits[0]);
    } finally {
      preview.removeHandler();
      captured.removeFromParent();
      other.removeFromParent();
    }
  }

  @Test
  public void autoHideDetachesPopupAndUnregistersPreview() {
    PopupPanel popup = new PopupPanel(true);
    int[] closes = {0};
    popup.addCloseHandler(
        e -> {
          assertTrue(e.isAutoClosed());
          closes[0]++;
        });
    popup.show();
    mouseDown(com.google.gwt.dom.client.Document.get().getBody().unwrap());
    assertFalse(popup.isShowing());
    assertFalse(popup.isAttached());
    mouseDown(com.google.gwt.dom.client.Document.get().getBody().unwrap());
    assertEquals(1, closes[0]);
  }

  @JSBody(
      params = "element",
      script = "element.dispatchEvent(new MouseEvent('mousedown',{bubbles:true,cancelable:true}));")
  private static native void mouseDown(JSObject element);
}
