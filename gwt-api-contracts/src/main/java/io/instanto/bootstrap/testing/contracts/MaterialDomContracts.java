package io.instanto.bootstrap.testing.contracts;

import static io.instanto.bootstrap.testing.contracts.ContractAssertions.*;

import com.google.gwt.dom.client.*;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.ArrayList;
import java.util.List;

/** Typed DOM views must operate on the same browser nodes as generic widget elements. */
public final class MaterialDomContracts {
  private MaterialDomContracts() {}

  public static void typedResourceElementsRetainNativeProperties() {
    Document document = Document.get();
    ImageElement image = document.createImageElement();
    image.setWidth(143);
    image.setHeight(92);
    image.setAlt("A material illustration");
    ImageElement view = ImageElement.as(image);
    equal(143, view.getWidth(), "image width");
    equal(92, view.getHeight(), "image height");
    view.setAlt("Changed through second view");
    equal("Changed through second view", image.getAlt(), "image views share native state");
    equal("143", image.getAttribute("width"), "image width reflected as attribute");
    LinkElement link = document.createLinkElement();
    link.setRel("stylesheet");
    link.setMedia("print");
    equal("stylesheet", link.getAttribute("rel"), "stylesheet relation");
    equal("print", LinkElement.as(link).getMedia(), "stylesheet media");
    StyleElement style = document.createStyleElement();
    style.setType("text/css");
    style.setMedia("screen");
    style.setInnerText(".material-contract { color: red; }");
    equal("screen", StyleElement.as(style).getMedia(), "style media");
    equal(".material-contract { color: red; }", style.getInnerText(), "stylesheet content");
    SourceElement source = document.createSourceElement();
    source.setType("video/mp4");
    equal("video/mp4", source.getAttribute("type"), "media source type");
    IFrameElement frame = document.createIFrameElement();
    frame.setName("material-preview");
    equal("material-preview", frame.getAttribute("name"), "frame name");
  }

  public static void optionViewsTrackInsertionSelectionAndDisabledState() {
    Document document = Document.get();
    SelectElement select = document.createSelectElement();
    OptionElement first = document.createOptionElement();
    first.setText("First option");
    first.setValue("first");
    first.setLabel("First label");
    first.setDefaultSelected(true);
    select.add(first, null);
    OptionElement second = document.createOptionElement();
    second.setText("Second option");
    second.setValue("second");
    select.add(second, first);
    equal(1, first.getIndex(), "insertion updates original option index");
    equal(0, second.getIndex(), "inserted option index");
    equal("First option", select.getOptions().getItem(1).getText(), "live option view text");
    equal(
        "First label",
        select.getOptions().getItem(1).getLabel(),
        "option label distinct from text");
    isTrue(first.isDefaultSelected(), "default selection retained");
    second.setSelected(true);
    isTrue(second.isSelected(), "live selection changes");
    isTrue(!first.isSelected(), "single selection clears prior selection");
    isTrue(first.isDefaultSelected(), "live selection preserves default");
    select.getOptions().getItem(0).setDisabled(true);
    isTrue(second.isDisabled(), "disabled state shared across wrappers");
    second.removeFromParent();
    equal(0, first.getIndex(), "removal updates remaining index");
    OptGroupElement group = document.createOptGroupElement();
    group.setLabel("Choices");
    group.setDisabled(true);
    group.appendChild(first);
    select.appendChild(group);
    equal("Choices", OptGroupElement.as(group).getLabel(), "group label");
    isTrue(group.isDisabled(), "group disabled state");
    equal(1, select.getOptions().getLength(), "options include grouped descendants");
  }

  public static void attachHandlerInterfaceReportsWidgetLifecycle() {
    Label widget = new Label("lifecycle");
    HasAttachHandlers source = widget;
    List<Boolean> calls = new ArrayList<>();
    var registration = source.addAttachHandler(event -> calls.add(event.isAttached()));
    RootPanel.get().add(widget);
    widget.removeFromParent();
    registration.removeHandler();
    RootPanel.get().add(widget);
    widget.removeFromParent();
    equal("[true, false]", calls.toString(), "attach interface and listener removal");
  }
}
