package com.google.gwt.dom.client;

import org.teavm.jso.dom.html.HTMLElement;

/** A typed view of an HTML video element; media loading remains browser-owned. */
public class VideoElement extends Element {
  public void play() {
    playNative(unwrap());
  }

  public void pause() {
    pauseNative(unwrap());
  }

  @org.teavm.jso.JSBody(params = "video", script = "video.play();")
  private static native void playNative(org.teavm.jso.dom.html.HTMLElement video);

  @org.teavm.jso.JSBody(params = "video", script = "video.pause();")
  private static native void pauseNative(org.teavm.jso.dom.html.HTMLElement video);

  public static final String TAG = "video";

  public VideoElement(HTMLElement element) {
    super(element);
  }

  public static VideoElement as(Element e) {
    if (e == null || !TAG.equalsIgnoreCase(e.getTagName()))
      throw new IllegalArgumentException("Expected video");
    return new VideoElement(e.unwrap());
  }

  public boolean isLoop() {
    return getPropertyBoolean("loop");
  }

  public void setLoop(boolean value) {
    setPropertyBoolean("loop", value);
  }

  public boolean isAutoplay() {
    return getPropertyBoolean("autoplay");
  }

  public void setAutoplay(boolean value) {
    setPropertyBoolean("autoplay", value);
  }

  public boolean isMuted() {
    return getPropertyBoolean("muted");
  }

  public void setMuted(boolean value) {
    setPropertyBoolean("muted", value);
  }

  public boolean isControls() {
    return getPropertyBoolean("controls");
  }

  public void setControls(boolean value) {
    setPropertyBoolean("controls", value);
  }

  public String getPoster() {
    return getPropertyString("poster");
  }

  public void setPoster(String value) {
    setPropertyString("poster", value);
  }

  public String getSrc() {
    return getPropertyString("src");
  }

  public void setSrc(String value) {
    setPropertyString("src", value);
  }

  public String getPreload() {
    return getPropertyString("preload");
  }

  public void setPreload(String value) {
    setPropertyString("preload", value);
  }

  public int getWidth() {
    return getPropertyInt("width");
  }

  public int getHeight() {
    return getPropertyInt("height");
  }

  public int getVideoWidth() {
    return getPropertyInt("videoWidth");
  }

  public int getVideoHeight() {
    return getPropertyInt("videoHeight");
  }

  public void setWidth(int v) {
    setPropertyInt("width", v);
  }

  public void setHeight(int v) {
    setPropertyInt("height", v);
  }
}
