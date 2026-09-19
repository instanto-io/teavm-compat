package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class CanvasAndArrayCompatibilityTest {
  @Test
  public void canvasWrapsAndExportsTheSameNativeElement() {
    Canvas canvas = Canvas.createIfSupported();
    assertNotNull(canvas);
    canvas.setCoordinateSpaceWidth(80);
    canvas.setCoordinateSpaceHeight(40);
    assertEquals(80, canvas.getCoordinateSpaceWidth());
    assertEquals(40, canvas.getCoordinateSpaceHeight());
    assertTrue(CanvasElement.is(canvas.getElement()));
    assertEquals(canvas.getElement().unwrap(), CanvasElement.as(canvas.getElement()).unwrap());
    canvas.getContext2d().scale(2, 3);
    assertEquals(2, scaleX(canvas.getContext2d().unwrap()), 0);
    assertTrue(canvas.toDataUrl().startsWith("data:image/png;base64,"));
  }

  @Test
  public void typedArrayFactoryRetainsStringOperations() {
    JsArrayString values = JsArrayString.createArray().cast();
    values.push("first");
    values.push("second");
    values.set(0, "changed");
    assertEquals(2, values.length());
    assertEquals("changed", values.get(0));
    assertEquals("second", values.get(1));
    assertTrue(isArray(values.unwrap()));
  }

  @Test
  public void fileInputAndClearedWidthUseNativeProperties() {
    assertEquals("file", Document.get().createFileInputElement().getType());
    Element element = Document.get().createDivElement();
    element.getStyle().setProperty("width", "120px");
    element.getStyle().clearWidth();
    assertEquals("", element.getStyle().getProperty("width"));
  }

  @JSBody(params = "context", script = "return context.getTransform().a;")
  private static native double scaleX(JSObject context);

  @JSBody(params = "value", script = "return Array.isArray(value);")
  private static native boolean isArray(JSObject value);
}
