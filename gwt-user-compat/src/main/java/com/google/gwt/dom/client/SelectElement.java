package com.google.gwt.dom.client;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLElement;

public class SelectElement extends Element {
  public SelectElement(HTMLElement element) {
    super(element);
  }

  public static SelectElement as(Element element) {
    return new SelectElement(element.unwrap());
  }

  public NodeList<OptionElement> getOptions() {
    return new NodeList<>(options(unwrap()), OptionElement::new);
  }

  public boolean isMultiple() {
    return getPropertyBoolean("multiple");
  }

  public void add(OptionElement option, OptionElement before) {
    addOption(unwrap(), option.unwrap(), before == null ? null : before.unwrap());
  }

  @JSBody(
      params = {"select", "option", "before"},
      script = "select.add(option, before);")
  private static native void addOption(JSObject select, JSObject option, JSObject before);

  public void setMultiple(boolean value) {
    setPropertyBoolean("multiple", value);
  }

  @JSBody(params = "e", script = "return e.options;")
  private static native JSObject options(JSObject e);
}
