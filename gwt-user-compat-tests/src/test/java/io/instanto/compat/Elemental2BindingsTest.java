package io.instanto.compat;

import static org.junit.Assert.*;

import elemental2.dom.DomGlobal;
import elemental2.indexeddb.*;
import elemental2.media.*;
import elemental2.svg.*;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.core.JSPromise;
import org.teavm.jso.core.JSString;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class Elemental2BindingsTest {
  @Test
  public void svgPropertiesGeometryAndTransformsUseNativeObjects() {
    SVGSVGElement svg =
        (SVGSVGElement) DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "svg");
    SVGRectElement rect =
        (SVGRectElement) DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "rect");
    svg.appendChild(rect);
    DomGlobal.document.body.appendChild(svg);
    try {
      rect.x.baseVal.value = 5;
      rect.y.baseVal.value = 7;
      rect.width.baseVal.value = 40;
      rect.height.baseVal.value = 20;
      SVGRect bounds = rect.getBBox();
      assertEquals(40, bounds.width, 0.001);
      assertEquals(7, bounds.y, 0.001);
      SVGPoint point = svg.createSVGPoint();
      point.x = 2;
      point.y = 3;
      SVGMatrix matrix = svg.createSVGMatrix().translate(10, 20);
      SVGPoint moved = point.matrixTransform(matrix);
      assertEquals(12, moved.x, 0.001);
      assertEquals(23, moved.y, 0.001);
      SVGPathElement path =
          (SVGPathElement) DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "path");
      path.setAttribute("d", "M0 0 L30 40");
      svg.appendChild(path);
      assertEquals(50, path.getTotalLength(), 0.001);
    } finally {
      svg.remove();
    }
  }

  @Test
  public void indexedDbCommitsAndSharesRecordsWithTeaVmApi() {
    String name = "instanto-elemental2-" + System.currentTimeMillis();
    IDBOpenDBRequest opening = IndexedDbGlobal.indexedDB.open(name, 1);
    opening.onupgradeneeded =
        event -> {
          IDBDatabase database = Js.uncheckedCast(opening.result);
          database.createObjectStore("records").createIndex("byName", "name");
          return null;
        };
    IDBDatabase database = Js.uncheckedCast(request(opening).await());
    try {
      IDBTransaction writing = database.transaction("records", "readwrite");
      JSPromise<Void> committed = completion(writing);
      JsPropertyMap<Object> record = JsPropertyMap.of();
      record.set("name", "shared record");
      writing.objectStore("records").put(record, "one");
      committed.await();
      IDBTransaction reading = database.transaction("records", "readonly");
      Object result =
          request(reading.objectStore("records").index("byName").get("shared record")).await();
      assertEquals("shared record", Js.asString(Js.asPropertyMap(result).get("name")));

      // Read the same record through TeaVM's native IndexedDB API.
      var nativeDatabase = (org.teavm.jso.indexeddb.IDBDatabase) (Object) database;
      var nativeRequest =
          nativeDatabase
              .transaction("records", "readonly")
              .objectStore("records")
              .get(JSString.valueOf("one"));
      String nativeName =
          new JSPromise<String>(
                  (resolve, reject) -> {
                    nativeRequest.setOnSuccess(
                        () ->
                            resolve.accept(
                                Js.asString(
                                    Js.asPropertyMap(nativeRequest.getResult()).get("name"))));
                    nativeRequest.setOnError(
                        () ->
                            reject.accept(
                                new IllegalStateException("TeaVM IndexedDB read failed")));
                  })
              .await();
      assertEquals("shared record", nativeName);
      IDBTransaction deleting = database.transaction("records", "readwrite");
      JSPromise<Void> deleted = completion(deleting);
      deleting.objectStore("records").delete("one");
      deleted.await();
      Object count =
          request(database.transaction("records", "readonly").objectStore("records").count())
              .await();
      assertEquals(0, Js.asInt(count));
    } finally {
      database.close();
      request(IndexedDbGlobal.indexedDB.deleteDatabase(name)).await();
    }
  }

  @Test
  public void offlineAudioRendersTypedSamples() {
    OfflineAudioContext context = new OfflineAudioContext(1, 128, 44100);
    AudioBuffer buffer = context.createBuffer(1, 128, 44100);
    buffer.getChannelData(0).setAt(0, 0.5);
    AudioBufferSourceNode source = context.createBufferSource();
    source.buffer = buffer;
    source.connect(context.destination);
    source.start();
    AudioBuffer rendered = context.startRendering().<JSPromise<AudioBuffer>>cast().await();
    assertEquals(128, rendered.length);
    assertEquals(0.5, rendered.getChannelData(0).getAt(0), 0.0001);
  }

  private static JSPromise<Object> request(IDBRequest<?> request) {
    return new JSPromise<>(
        (resolve, reject) -> {
          request.onsuccess =
              event -> {
                resolve.accept(request.result);
                return null;
              };
          request.onerror =
              event -> {
                reject.accept(new IllegalStateException("IndexedDB request failed"));
                return null;
              };
        });
  }

  private static JSPromise<Void> completion(IDBTransaction transaction) {
    return new JSPromise<>(
        (resolve, reject) -> {
          transaction.oncomplete =
              event -> {
                resolve.accept(null);
                return null;
              };
          transaction.onabort =
              event -> {
                reject.accept(new IllegalStateException("IndexedDB transaction aborted"));
                return null;
              };
          transaction.onerror =
              event -> {
                reject.accept(new IllegalStateException("IndexedDB transaction failed"));
                return null;
              };
        });
  }
}
