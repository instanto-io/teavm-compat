package com.google.gwt.dom.client;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLElement;

/** Typed option view; identity follows the DOM node, not the wrapper. */
public class OptionElement extends Element {
  public static final String TAG = "option";

  public int getIndex() {
    return getPropertyInt("index");
  }

  public String getLabel() {
    return getPropertyString("label");
  }

  public void setLabel(String value) {
    setPropertyString("label", value);
  }

  public String getText() {
    return getPropertyString("text");
  }

  public void setText(String value) {
    setPropertyString("text", value);
  }

  public boolean isDefaultSelected() {
    return getPropertyBoolean("defaultSelected");
  }

  public void setDefaultSelected(boolean value) {
    setPropertyBoolean("defaultSelected", value);
  }

  public boolean isDisabled() {
    return getPropertyBoolean("disabled");
  }

  public void setDisabled(boolean value) {
    setPropertyBoolean("disabled", value);
  }

  private static final JSObject IDENTITY_FOR_ELEMENT = createIdentityLookup();

  public OptionElement(HTMLElement element) {
    super(element);
  }

  public static OptionElement as(Element element) {
    return new OptionElement(element.unwrap());
  }

  public boolean isSelected() {
    return getPropertyBoolean("selected");
  }

  public void setSelected(boolean value) {
    setPropertyBoolean("selected", value);
  }

  public String getValue() {
    return getPropertyString("value");
  }

  public void setValue(String value) {
    setPropertyString("value", value);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof OptionElement && same(unwrap(), ((OptionElement) other).unwrap());
  }

  @Override
  public int hashCode() {
    return identity(IDENTITY_FOR_ELEMENT, unwrap());
  }

  @JSBody(
      script =
          """
          const identities = new WeakMap();
          let next = 0;
          return element => {
            if (!identities.has(element)) {
              next = (next + 1) | 0;
              identities.set(element, next);
            }
            return identities.get(element);
          };
          """)
  private static native JSObject createIdentityLookup();

  @JSBody(
      params = {"a", "b"},
      script = "return a === b;")
  private static native boolean same(JSObject a, JSObject b);

  @JSBody(
      params = {"lookup", "element"},
      script = "return lookup(element);")
  private static native int identity(JSObject lookup, JSObject element);
}
