package com.google.gwt.dom.client;

import com.google.gwt.canvas.dom.client.Context2d;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLElement;

/** GWT canvas element backed by the browser's native canvas. */
public class CanvasElement extends Element {
  public static final String TAG = "canvas";

  public CanvasElement(HTMLElement element) {
    super(element);
  }

  public static CanvasElement as(Element element) {
    if (!is(element)) throw new IllegalArgumentException("Expected a canvas element");
    return new CanvasElement(element.unwrap());
  }

  public static boolean is(Element element) {
    return element != null && TAG.equalsIgnoreCase(element.getTagName());
  }

  public int getWidth() {
    return getPropertyInt("width");
  }

  public int getHeight() {
    return getPropertyInt("height");
  }

  public void setWidth(int value) {
    setPropertyInt("width", value);
  }

  public void setHeight(int value) {
    setPropertyInt("height", value);
  }

  public Context2d getContext2d() {
    JSObject context = context(unwrap());
    return context == null ? null : new Context2d(context);
  }

  public String toDataUrl() {
    return toDataUrl("image/png");
  }

  public String toDataUrl(String type) {
    return dataUrl(unwrap(), type);
  }

  @JSBody(params = "element", script = "return element.getContext('2d');")
  private static native JSObject context(HTMLElement element);

  @JSBody(
      params = {"element", "type"},
      script = "return element.toDataURL(type);")
  private static native String dataUrl(HTMLElement element, String type);
}
