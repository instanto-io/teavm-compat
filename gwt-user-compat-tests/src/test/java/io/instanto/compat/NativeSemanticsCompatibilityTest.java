package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.*;
import com.google.gwt.geolocation.client.*;
import com.google.gwt.typedarrays.shared.*;
import com.google.gwt.user.client.ui.Frame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.*;
import org.teavm.junit.*;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class NativeSemanticsCompatibilityTest {
  @Test
  public void absentAttributesUseGwtEmptyStrings() {
    Element element = Document.get().createDivElement();
    assertEquals("", element.getAttribute("href"));
    element.setAttribute("href", "#material");
    assertEquals("#material", element.getAttribute("href"));
    element.removeAttribute("href");
    assertEquals("", element.getAttribute("href"));
  }

  @Test
  public void missingNativeValuesBecomeNull() {
    assertNull(JavaScriptObject.of(undefined()));
  }

  @JSBody(script = "return undefined;")
  private static native JSObject undefined();

  @Test
  public void primaryStylesFollowDomClassesAndRenameDependents() {
    com.google.gwt.user.client.ui.Label label = new com.google.gwt.user.client.ui.Label();
    label.setStyleName("material secondary");
    label.addStyleDependentName("disabled");
    assertEquals("material", label.getStylePrimaryName());
    assertTrue(label.getElement().hasClassName("material-disabled"));
    label.setStylePrimaryName("replacement");
    assertEquals("replacement secondary replacement-disabled", label.getStyleName());
    label.setStyleName("");
    label.addStyleDependentName("disabled");
    assertEquals("-disabled", label.getStyleName());
  }

  @Test
  public void documentTitleUsesTheHostDocument() {
    String previous = Document.get().getTitle();
    try {
      Document.get().setTitle("Material catalogue");
      assertEquals("Material catalogue", hostTitle());
    } finally {
      Document.get().setTitle(previous);
    }
  }

  @JSBody(script = "return document.title;")
  private static native String hostTitle();

  @Test
  public void opaqueDomValuesRetainTheirTypedView() {
    InputElement input = Document.get().createInputElement("text");
    JavaScriptObject opaque = JavaScriptObject.of(input.unwrap());
    InputElement restored = opaque.cast();
    restored.setValue("restored");
    assertEquals("restored", input.getValue());
    assertSame(restored, Element.as(opaque));
    input.setPropertyObject("disabled", Boolean.TRUE);
    assertTrue(input.getPropertyBoolean("disabled"));
    input.setPropertyObject("disabled", Boolean.FALSE);
    assertFalse(input.getPropertyBoolean("disabled"));
  }

  @Test
  public void frameAndVideoExposeNativeProperties() {
    Frame frame = new Frame("about:blank");
    assertEquals("IFRAME", frame.getElement().getTagName());
    assertEquals("about:blank", frame.getUrl());
    VideoElement video = Document.get().createVideoElement();
    video.setControls(true);
    video.setMuted(true);
    video.setLoop(true);
    assertTrue(video.isControls());
    assertTrue(video.isMuted());
    assertTrue(video.isLoop());
    assertTrue(JavaScriptObject.of(video.unwrap()) instanceof VideoElement);
  }

  @Test
  public void byteArraysHaveUnsignedValuesAndSharedSubarrays() {
    ArrayBuffer buffer = TypedArrays.createArrayBuffer(8);
    Uint8Array bytes = TypedArrays.createUint8Array(buffer, 2, 4);
    bytes.set(0, 511);
    bytes.set(1, -1);
    assertEquals(255, bytes.get(0));
    assertEquals(255, bytes.get(1));
    assertEquals(2, bytes.byteOffset());
    assertEquals(4, bytes.length());
    Uint8Array tail = bytes.subarray(1, 3);
    tail.set(0, 17);
    assertEquals(17, bytes.get(1));
    assertEquals(8, buffer.byteLength());
  }

  @Test
  public void geolocationDelegatesOptionsResultsErrorsAndWatchDisposal() {
    JSObject saved = mockLocation();
    try {
      Geolocation geo = Geolocation.getIfSupported();
      assertNotNull(geo);
      int[] outcomes = {0};
      Callback<Position, PositionError> callback =
          new Callback<>() {
            public void onSuccess(Position p) {
              assertEquals(51.5, p.getCoordinates().getLatitude(), 0.001);
              assertNull(p.getCoordinates().getAltitude());
              assertEquals(123, p.getTimestamp(), 0);
              outcomes[0]++;
            }

            public void onFailure(PositionError e) {
              assertEquals(1, e.getCode());
              outcomes[0] += 10;
            }
          };
      geo.getCurrentPosition(
          callback,
          new Geolocation.PositionOptions()
              .setHighAccuracyEnabled(true)
              .setMaximumAge(20)
              .setTimeout(30));
      assertEquals("true|20|30", locationOptions());
      int watch = geo.watchPosition(callback);
      assertEquals(42, watch);
      geo.clearWatch(watch);
      assertEquals(11, outcomes[0]);
      assertEquals(42, clearedWatch());
    } finally {
      restoreLocation(saved);
    }
  }

  @JSBody(
      script =
          "var saved={descriptor:Object.getOwnPropertyDescriptor(navigator,'geolocation')}; Object.defineProperty(navigator,'geolocation',{configurable:true,value:{getCurrentPosition:function(ok,err,options){this.options=options;ok({timestamp:123,coords:{latitude:51.5,longitude:0,accuracy:2,altitude:null}});},watchPosition:function(ok,err){err({code:1,message:'fixture denial'});return 42;},clearWatch:function(id){this.cleared=id;}}});return saved;")
  private static native JSObject mockLocation();

  @JSBody(
      script =
          "var o=navigator.geolocation.options;return [o.enableHighAccuracy,o.maximumAge,o.timeout].join('|');")
  private static native String locationOptions();

  @JSBody(script = "return navigator.geolocation.cleared;")
  private static native int clearedWatch();

  @JSBody(
      params = "saved",
      script =
          "if(saved.descriptor)Object.defineProperty(navigator,'geolocation',saved.descriptor);else delete navigator.geolocation;")
  private static native void restoreLocation(JSObject saved);
}
