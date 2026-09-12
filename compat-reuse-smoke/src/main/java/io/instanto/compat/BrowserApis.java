package io.instanto.compat;

import elemental2.core.JsArray;
import elemental2.dom.*;
import elemental2.promise.Promise;
import elemental2.webstorage.WebStorageWindow;
import jsinterop.base.JsPropertyMap;
import org.gwtproject.i18n.shared.cldr.impl.BrowserDateTimeFormatInfo;

/** Browser contracts exercise the compatibility layer beyond reachable widget declarations. */
public final class BrowserApis {
  public static HTMLElement render() {
    HTMLElement root = (HTMLElement) DomGlobal.document.createElement("section");
    root.id = "browser-apis";
    BrowserDateTimeFormatInfo spanish = new BrowserDateTimeFormatInfo("es");
    root.setAttribute("data-spanish", spanish.monthsFull()[0]);
    root.setAttribute("data-arabic", new BrowserDateTimeFormatInfo("ar").monthsFull()[0]);
    var dateFormat = org.gwtproject.i18n.shared.DateTimeFormat.getFormat("yyyy-MM-dd");
    BindingContracts.check(
        "2024-02-29".equals(dateFormat.format(dateFormat.parseStrict("2024-02-29"))),
        "leap date roundtrip");
    boolean rejected = false;
    try {
      dateFormat.parseStrict("2024-02-30");
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    BindingContracts.check(rejected, "invalid calendar date rejected");
    root.setAttribute("data-date-parse", "passed");
    for (var storage :
        new elemental2.webstorage.Storage[] {
          WebStorageWindow.of(DomGlobal.window).localStorage,
          WebStorageWindow.of(DomGlobal.window).sessionStorage
        }) {
      storage.setItem("domino-compat-probe", "native-value");
      BindingContracts.check(
          "native-value".equals(storage.getItem("domino-compat-probe")), "storage roundtrip");
      storage.removeItem("domino-compat-probe");
      BindingContracts.check(storage.getItem("domino-compat-probe") == null, "storage deletion");
    }
    root.setAttribute("data-storage", "passed");
    Element svg = DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("width", "80");
    svg.setAttribute("height", "40");
    Element rect = DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "rect");
    rect.setAttribute("width", "80");
    rect.setAttribute("height", "40");
    rect.setAttribute("fill", "#4466cc");
    svg.appendChild(rect);
    root.appendChild(svg);
    Promise.resolve("promise-value")
        .then(
            value -> {
              root.setAttribute("data-promise", value.toUpperCase());
              return null;
            });
    Promise.reject("rejected-value")
        .catch_(
            value -> {
              root.setAttribute("data-rejection", (String) value);
              return null;
            });
    JsArray<Blob.ConstructorBlobPartsArrayUnionType> parts = new JsArray<>();
    parts.push(Blob.ConstructorBlobPartsArrayUnionType.of("blob-value"));
    Blob blob = new Blob(parts);
    String blobUrl = URL.createObjectURL(blob);
    DomGlobal.fetch(blobUrl)
        .then(response -> response.text())
        .then(
            value -> {
              root.setAttribute("data-blob", value);
              URL.revokeObjectURL(blobUrl);
              return null;
            });
    HTMLInputElement file = (HTMLInputElement) DomGlobal.document.createElement("input");
    file.type = "file";
    file.id = "native-upload";
    file.setAttribute("aria-label", "Upload text");
    file.addEventListener(
        "change",
        e -> {
          FileReader reader = new FileReader();
          reader.onload =
              event -> {
                root.setAttribute("data-file", reader.result.asString());
                return null;
              };
          reader.readAsText(file.files.item(0));
        });
    root.appendChild(file);
    HTMLButtonElement history = (HTMLButtonElement) DomGlobal.document.createElement("button");
    history.id = "push-history";
    history.textContent = "Push history state";
    history.addEventListener(
        "click",
        e -> {
          JsPropertyMap<String> state = JsPropertyMap.of();
          state.set("page", "detail");
          DomGlobal.history.pushState(state, "", "?page=browser-apis#detail");
          root.setAttribute("data-history", "detail");
        });
    root.appendChild(history);
    DomGlobal.window.addEventListener("popstate", e -> root.setAttribute("data-history", "back"));
    root.setAttribute("data-ready", "true");
    return root;
  }
}
