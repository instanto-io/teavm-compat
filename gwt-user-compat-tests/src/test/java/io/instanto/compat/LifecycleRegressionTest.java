package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.impl.*;
import com.google.gwt.view.client.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.*;
import org.teavm.junit.*;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class LifecycleRegressionTest {
  @Test
  public void hidingPopupDetachesAndRemovesIt() {
    PopupPanel popup = new PopupPanel();
    popup.show();
    try {
      popup.hide();
      assertFalse("hidden popup must detach", popup.isAttached());
      assertNull(popup.getParent());
    } finally {
      popup.removeFromParent();
    }
  }

  @Test
  public void removedDisplayStopsRequestingData() {
    CellList<String> display = new CellList<>(new TextCell());
    int[] calls = {0};
    AbstractDataProvider<String> provider =
        new AbstractDataProvider<>() {
          protected void onRangeChanged(HasData<String> ignored) {
            calls[0]++;
          }
        };
    provider.addDataDisplay(display);
    provider.removeDataDisplay(display);
    int before = calls[0];
    display.setVisibleRange(1, 2);
    assertEquals("removed display must no longer notify provider", before, calls[0]);
  }

  @Test
  public void shorterDataSetClearsOldRows() {
    CellList<String> display = new CellList<>(new TextCell());
    ListDataProvider<String> provider =
        new ListDataProvider<>(new ArrayList<>(List.of("A", "B", "C")));
    provider.addDataDisplay(display);
    provider.setList(new ArrayList<>(List.of("D")));
    assertEquals(
        "rows beyond the new data set must be discarded", 1, display.getVisibleItemCount());
  }

  @Test
  public void dataOutsidePageDoesNotAllocateVisibleRows() {
    CellList<String> display = new CellList<>(new TextCell());
    display.setVisibleRange(0, 2);
    display.setRowData(1000, List.of("outside"));
    assertEquals(
        "non-overlapping updates must not extend visible page", 0, display.getVisibleItemCount());
  }

  static class BadUnload extends Label {
    boolean fail = true;

    @Override
    protected void onUnload() {
      if (fail) throw new IllegalStateException("test unload");
    }
  }

  @Test
  public void failedUnloadStillUnparentsChild() {
    FlowPanel parent = new FlowPanel();
    BadUnload child = new BadUnload();
    parent.add(child);
    RootPanel.get().add(parent);
    try {
      try {
        parent.remove(child);
      } catch (RuntimeException expected) {
        // The assertions below verify cleanup after the deliberately failing callback.
      }
      assertNull("exception must not leave stale parent ownership", child.getParent());
      assertEquals(0, parent.getWidgetCount());
    } finally {
      child.fail = false;
      parent.removeFromParent();
    }
  }

  @Test
  public void clearingPanelContinuesAfterAChildUnloadFails() {
    FlowPanel parent = new FlowPanel();
    BadUnload first = new BadUnload();
    Label second = new Label();
    parent.add(first);
    parent.add(second);
    RootPanel.get().add(parent);
    try {
      try {
        parent.clear();
        fail("expected aggregated unload failure");
      } catch (UmbrellaException expected) {
        assertEquals(1, expected.getCauses().size());
      }
      assertEquals(0, parent.getWidgetCount());
      assertNull(first.getParent());
      assertNull(second.getParent());
      assertFalse(first.isAttached());
      assertFalse(second.isAttached());
    } finally {
      first.fail = false;
      parent.removeFromParent();
    }
  }

  @Test
  public void duplicateDisplayDoesNotAddAnotherSubscription() {
    CellList<String> display = new CellList<>(new TextCell());
    int[] calls = {0};
    AbstractDataProvider<String> provider =
        new AbstractDataProvider<>() {
          protected void onRangeChanged(HasData<String> ignored) {
            calls[0]++;
          }
        };
    provider.addDataDisplay(display);
    try {
      provider.addDataDisplay(display);
      fail("duplicate display accepted");
    } catch (IllegalStateException expected) {
      // Duplicate registration must fail without adding a second subscription.
    }
    display.setVisibleRange(1, 2);
    assertEquals(2, calls[0]);
    provider.removeDataDisplay(display);
  }

  interface H extends EventHandler {
    void accept(E event);
  }

  static class E extends GwtEvent<H> {
    static final Type<H> TYPE = new Type<>();

    public Type<H> getAssociatedType() {
      return TYPE;
    }

    protected void dispatch(H handler) {
      handler.accept(this);
    }
  }

  @Test
  public void handlerFailureDoesNotSkipLaterHandlers() {
    HandlerManager manager = new HandlerManager(new Object());
    int[] calls = {0};
    manager.addHandler(
        E.TYPE,
        e -> {
          throw new IllegalStateException("test handler");
        });
    manager.addHandler(E.TYPE, e -> calls[0]++);
    try {
      manager.fireEvent(new E());
    } catch (RuntimeException expected) {
      // Dispatch must notify the remaining handler before propagating this failure.
    }
    assertEquals("dispatch all handlers before propagating failures", 1, calls[0]);
  }

  @Test
  public void unhookingFormActuallyRemovesListeners() {
    FormElement form = Document.get().createFormElement();
    int[] submits = {0};
    FormPanelImplHost host =
        new FormPanelImplHost() {
          public boolean onFormSubmit() {
            submits[0]++;
            return false;
          }

          public void onFrameLoad() {
            // This fixture tests submit unregistration and has no iframe.
          }
        };
    FormPanelImpl impl = new FormPanelImpl();
    impl.hookEvents(null, form, host);
    impl.unhookEvents(null, form);
    dispatchSubmit(form.unwrap());
    assertEquals("unhooked form must not retain or call host", 0, submits[0]);
  }

  @JSBody(params = "form", script = "form.dispatchEvent(new Event('submit', {cancelable:true}));")
  static native void dispatchSubmit(JSObject form);

  @Test
  public void replacedRootReleasesOldDetachRegistration() {
    Element original = Document.get().createDivElement();
    original.setId("review-root");
    Document.get().getBody().appendChild(original);
    RootPanel oldRoot = RootPanel.get("review-root");
    original.removeFromParent();
    Element replacement = Document.get().createDivElement();
    replacement.setId("review-root");
    Document.get().getBody().appendChild(replacement);
    RootPanel newRoot = RootPanel.get("review-root");
    try {
      assertFalse(
          "replaced root is still retained by global detach set",
          RootPanel.isInDetachList(oldRoot));
      assertFalse(oldRoot.isAttached());
    } finally {
      RootPanel.detachNow(oldRoot);
      RootPanel.detachNow(newRoot);
      replacement.removeFromParent();
    }
  }
}
