package io.instanto.bootstrap.testing.teavm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import io.instanto.bootstrap.testing.contracts.BrowserBoundGwtContracts;
import io.instanto.bootstrap.testing.contracts.JvmSafeGwtContracts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class TeaVmGwtCompatibilityContractsTest {
  @Test
  public void moduleBaseUrlCanBeSuppliedByTheTeaVmHost() {
    final String original = GWT.getModuleBaseURL();
    try {
      GWT.setModuleBaseURL("showcases/teavm");
      assertEquals("showcases/teavm/", GWT.getModuleBaseURL());
    } finally {
      GWT.setModuleBaseURL(original);
    }
  }

  @Test
  public void safeHtmlEscapesAndPreservesTrustedFragments() {
    JvmSafeGwtContracts.safeHtmlEscapesAndPreservesTrustedFragments();
  }

  @Test
  public void handlerManagerPreservesOrderAndSource() {
    JvmSafeGwtContracts.handlerManagerPreservesOrderAndSource();
  }

  @Test
  public void handlerMutationIsDeferredUntilTheNextDispatch() {
    JvmSafeGwtContracts.handlerMutationIsDeferredUntilTheNextDispatch();
  }

  @Test
  public void removedHandlerDoesNotReceiveLaterEvents() {
    JvmSafeGwtContracts.removedHandlerDoesNotReceiveLaterEvents();
  }

  @Test
  public void valueChangesRespectExplicitEventSuppression() {
    JvmSafeGwtContracts.valueChangesRespectExplicitEventSuppression();
  }

  @Test
  public void specializedElementFactoriesAndNarrowingRetainTheirTypes() {
    BrowserBoundGwtContracts.specializedElementFactoriesAndNarrowingRetainTheirTypes();
  }

  @Test
  public void mouseEventTargetsRetainTheirUnderlyingElements() {
    Element related = Document.get().createDivElement();
    NativeEvent event =
        Document.get().createMouseOverEvent(1, 2, 3, 4, 5, false, false, false, false, 0, related);

    EventTarget relatedTarget = event.getRelatedEventTarget();
    assertNotNull("related event target must be present", relatedTarget);

    Element narrowed = Element.as(relatedTarget);
    narrowed.setAttribute("data-related-target", "retained");
    assertEquals(
        "Element.as must retain the related target's underlying element",
        "retained",
        related.getAttribute("data-related-target"));
  }

  @Test
  public void widgetLifecycleReportsAttachThenDetachAndClearsParent() {
    BrowserBoundGwtContracts.widgetLifecycleReportsAttachThenDetachAndClearsParent();
  }

  @Test
  public void dependentStyleNamesFollowThePrimaryStyle() {
    BrowserBoundGwtContracts.dependentStyleNamesFollowThePrimaryStyle();
  }

  @Test
  public void checkboxValueBridgeUsesTheSubclassExtensionPointOnce() {
    BrowserBoundGwtContracts.checkboxValueBridgeUsesTheSubclassExtensionPointOnce();
  }

  @Test
  public void rootPanelsAreCachedPerElement() {
    final Label host = new Label();
    host.getElement().setId("contract-root-host");
    RootPanel.get().add(host);
    try {
      final RootPanel panel = RootPanel.get("contract-root-host");
      assertNotNull(panel);
      assertSame(panel, RootPanel.get("contract-root-host"));
      panel.add(new Label("kept"));
      assertEquals(1, RootPanel.get("contract-root-host").getWidgetCount());
    } finally {
      host.removeFromParent();
    }
  }

  @Test
  public void aReplacedElementGetsItsOwnRootPanel() {
    final Label first = new Label();
    first.getElement().setId("contract-replaced-host");
    RootPanel.get().add(first);
    final RootPanel before = RootPanel.get("contract-replaced-host");
    first.removeFromParent();

    final Label second = new Label();
    second.getElement().setId("contract-replaced-host");
    RootPanel.get().add(second);
    try {
      assertNotSame(before, RootPanel.get("contract-replaced-host"));
    } finally {
      second.removeFromParent();
    }
  }

  @Test
  public void aWrappingPanelIsDetachedWhenThePageCloses() {
    final Label host = new Label();
    host.getElement().setId("contract-detach-host");
    RootPanel.get().add(host);
    try {
      final RootPanel panel = RootPanel.get("contract-detach-host");
      assertTrue(RootPanel.isInDetachList(panel));
      assertTrue(panel.isAttached());
      RootPanel.detachNow(panel);
      assertFalse(RootPanel.isInDetachList(panel));
      assertFalse(panel.isAttached());
    } finally {
      host.removeFromParent();
    }
  }
}
