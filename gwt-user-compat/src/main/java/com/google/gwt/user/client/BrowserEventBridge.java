package com.google.gwt.user.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import java.util.ArrayList;
import java.util.List;
import org.teavm.jso.*;

/** Owns document-wide preview/capture listeners only while they are needed. */
final class BrowserEventBridge {
  private static final List<Event.NativePreviewHandler> previews = new ArrayList<>();
  private static final JSObject listeners = newWeakMap();
  private static Element capture;
  private static JSObject documentRegistration;

  @JSFunctor
  interface NativeHandler extends JSObject {
    void handle(org.teavm.jso.dom.events.Event event);
  }

  static com.google.gwt.event.shared.HandlerRegistration preview(
      Event.NativePreviewHandler handler) {
    java.util.Objects.requireNonNull(handler);
    previews.add(handler);
    updateDocumentListener();
    return new com.google.gwt.event.shared.HandlerRegistration() {
      private Event.NativePreviewHandler registered = handler;

      public void removeHandler() {
        if (registered == null) return;
        previews.remove(registered);
        registered = null;
        updateDocumentListener();
      }
    };
  }

  static void setCapture(Element element) {
    capture = element;
    updateDocumentListener();
  }

  static Element getCapture() {
    return capture;
  }

  static void releaseCapture(Element element) {
    if (capture != null && capture.equals(element)) setCapture(null);
  }

  private static void updateDocumentListener() {
    if (capture != null || !previews.isEmpty()) {
      if (documentRegistration == null)
        documentRegistration = hookDocument(BrowserEventBridge::previewAndCapture);
    } else if (documentRegistration != null) {
      unhookDocument(documentRegistration);
      documentRegistration = null;
    }
  }

  private static void previewAndCapture(org.teavm.jso.dom.events.Event nativeEvent) {
    Scheduler.runEntry(
        () -> {
          Event event = new Event(nativeEvent);
          Event.NativePreviewEvent preview = new Event.NativePreviewEvent(event);
          List<Event.NativePreviewHandler> snapshot = new ArrayList<>(previews);
          for (int i = snapshot.size() - 1; i >= 0 && !preview.isConsumed(); i--) {
            snapshot.get(i).onPreviewNativeEvent(preview);
          }
          if (preview.isCanceled()) {
            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();
            return;
          }
          if (capture != null
              && (event.getType().startsWith("mouse")
                  || event.getType().equals("click")
                  || event.getType().equals("dblclick"))) {
            dispatchCaptured(listeners, capture.unwrap(), nativeEvent);
            nativeEvent.stopPropagation();
          }
        });
  }

  static void setListener(Element element, EventListener listener) {
    setListener(
        listeners,
        element.unwrap(),
        listener == null
            ? null
            : event -> Scheduler.runEntry(() -> listener.onBrowserEvent(new Event(event))));
  }

  static void sink(Element element, String name) {
    sink(listeners, element.unwrap(), name);
  }

  static int bits(Element element) {
    return bits(listeners, element.unwrap());
  }

  static void sinkBits(Element element, int bits) {
    int previous = bits(element);
    String[] names = {
      "blur",
      "change",
      "click",
      "dblclick",
      "focus",
      "keydown",
      "keypress",
      "keyup",
      "load",
      "mousedown",
      "mouseenter",
      "mouseleave",
      "mousemove",
      "mouseout",
      "mouseover",
      "mouseup",
      "mousewheel",
      "paste",
      "scroll",
      "touchstart",
      "touchmove",
      "touchend",
      "touchcancel"
    };
    for (String name : names) {
      int bit = Event.getTypeInt(name);
      if ((bits & bit) != 0) sink(element, name);
      else if ((previous & bit) != 0) unsink(listeners, element.unwrap(), name);
    }
    setBits(listeners, element.unwrap(), bits);
  }

  @JSBody(script = "return new WeakMap();")
  private static native JSObject newWeakMap();

  @JSBody(
      params = {"map", "element", "handler"},
      script =
          "var s=map.get(element);if(!s){s={events:{},bits:0};map.set(element,s);}s.handler=handler;")
  private static native void setListener(JSObject map, JSObject element, NativeHandler handler);

  @JSBody(
      params = {"map", "element", "name"},
      script =
          "var s=map.get(element);if(!s){s={events:{},bits:0};map.set(element,s);}if(!s.events[name]){var"
              + " f=function(e){if(s.handler)s.handler(e);};s.events[name]=f;element.addEventListener(name,f);}")
  private static native void sink(JSObject map, JSObject element, String name);

  @JSBody(
      params = {"map", "element", "name"},
      script =
          "var s=map.get(element);if(s&&s.events[name]){element.removeEventListener(name,s.events[name]);delete"
              + " s.events[name];}")
  private static native void unsink(JSObject map, JSObject element, String name);

  @JSBody(
      params = {"map", "element"},
      script = "var s=map.get(element);return s?s.bits:0;")
  private static native int bits(JSObject map, JSObject element);

  @JSBody(
      params = {"map", "element", "bits"},
      script = "var s=map.get(element);if(!s){s={events:{}};map.set(element,s);}s.bits=bits;")
  private static native void setBits(JSObject map, JSObject element, int bits);

  @JSBody(
      params = {"map", "element", "event"},
      script = "var s=map.get(element);if(s&&s.handler)s.handler(event);")
  private static native void dispatchCaptured(
      JSObject map, JSObject element, org.teavm.jso.dom.events.Event event);

  @JSBody(
      params = "handler",
      script =
          "var names=['click','dblclick','mousedown','mouseup','mousemove','mouseover','mouseout','keydown','keyup','keypress','touchstart','touchmove','touchend','touchcancel','focus','blur','contextmenu'];names.forEach(function(n){document.addEventListener(n,handler,true);});return"
              + " {names:names,handler:handler};")
  private static native JSObject hookDocument(NativeHandler handler);

  @JSBody(
      params = "state",
      script =
          "state.names.forEach(function(n){document.removeEventListener(n,state.handler,true);});")
  private static native void unhookDocument(JSObject state);
}
