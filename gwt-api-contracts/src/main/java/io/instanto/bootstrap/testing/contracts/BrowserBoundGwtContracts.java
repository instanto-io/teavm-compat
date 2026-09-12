package io.instanto.bootstrap.testing.contracts;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Shared contracts that require a GWT-compiled or TeaVM-compiled browser runtime. */
public final class BrowserBoundGwtContracts {
  private BrowserBoundGwtContracts() {}

  @CompatibilityContract(ContractRuntime.BROWSER_BOUND)
  public static void specializedElementFactoriesAndNarrowingRetainTheirTypes() {
    HeadElement head = Document.get().getHead();
    ContractAssertions.equal(
        "HEAD", head.getTagName().toUpperCase(), "getHead must return the document head");

    InputElement input = Document.get().createTextInputElement();
    input.setValue("typed");
    ContractAssertions.equal("text", input.getType(), "text input factory type");
    ContractAssertions.equal("typed", input.getValue(), "typed input value");

    Element genericInput = Document.get().createElement("input");
    InputElement narrowed = InputElement.as(genericInput);
    narrowed.setValue("narrowed");
    ContractAssertions.equal(
        "narrowed",
        genericInput.getPropertyString("value"),
        "InputElement.as must retain the underlying element");
  }

  @CompatibilityContract(ContractRuntime.BROWSER_BOUND)
  public static void widgetLifecycleReportsAttachThenDetachAndClearsParent() {
    Label label = new Label("lifecycle");
    List<Boolean> states = new ArrayList<>();
    label.addAttachHandler(event -> states.add(event.isAttached()));

    RootPanel.get().add(label);
    ContractAssertions.isTrue(label.isAttached(), "widget must be attached after mounting");
    ContractAssertions.same(RootPanel.get(), label.getParent(), "mounted widget parent");

    label.removeFromParent();
    ContractAssertions.isTrue(!label.isAttached(), "widget must be detached after removal");
    ContractAssertions.equal(null, label.getParent(), "removed widget parent");
    ContractAssertions.equal(
        Arrays.asList(true, false), states, "attach handlers must observe attach before detach");
  }

  @CompatibilityContract(ContractRuntime.BROWSER_BOUND)
  public static void dependentStyleNamesFollowThePrimaryStyle() {
    Label label = new Label("styles");
    label.setStylePrimaryName("contract-widget");
    label.addStyleDependentName("selected");
    ContractAssertions.isTrue(
        label.getStyleName().contains("contract-widget-selected"),
        "dependent style must derive from the primary style");

    label.removeStyleDependentName("selected");
    ContractAssertions.isTrue(
        !label.getStyleName().contains("contract-widget-selected"),
        "dependent style removal must derive from the primary style");
  }

  @CompatibilityContract(ContractRuntime.BROWSER_BOUND)
  public static void checkboxValueBridgeUsesTheSubclassExtensionPointOnce() {
    TrackingCheckBox checkBox = new TrackingCheckBox();
    checkBox.addValueChangeHandler(event -> {});
    checkBox.addValueChangeHandler(event -> {});
    ContractAssertions.equal(
        1,
        checkBox.initializations,
        "the value-change DOM bridge must be initialized once through the subclass hook");
  }

  private static final class TrackingCheckBox extends CheckBox {
    private int initializations;

    @Override
    protected void ensureDomEventHandlers() {
      initializations++;
      super.ensureDomEventHandlers();
    }
  }
}
