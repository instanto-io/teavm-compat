package com.google.gwt.canvas.dom.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.VideoElement;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/** Native 2D context operations used for canvas scaling and video-frame capture. */
public class Context2d extends JavaScriptObject {
  public Context2d(JSObject context) {
    super(context);
  }

  public void scale(double x, double y) {
    scaleNative(unwrap(), x, y);
  }

  public void drawImage(VideoElement video, double x, double y, double width, double height) {
    drawNative(unwrap(), video.unwrap(), x, y, width, height);
  }

  @JSBody(
      params = {"context", "x", "y"},
      script = "context.scale(x,y);")
  private static native void scaleNative(JSObject context, double x, double y);

  @JSBody(
      params = {"context", "video", "x", "y", "width", "height"},
      script = "context.drawImage(video,x,y,width,height);")
  private static native void drawNative(
      JSObject context, JSObject video, double x, double y, double width, double height);
}
