/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * %%
 * Reimplements, over TeaVM's JSO libraries, part of the GWT client API. Class,
 * method and package names follow GWT (https://github.com/gwtproject/gwt),
 * Copyright (C) The GWT Project Authors, licensed under the Apache License,
 * Version 2.0. No GWT source is included.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.google.gwt.dom.client;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public final class Document {
  private static final Document INSTANCE = new Document();

  private Document() {}

  public static Document get() {
    return INSTANCE;
  }

  /** A live view of matching elements in the host document. */
  public NodeList<Element> getElementsByTagName(final String tagName) {
    return new NodeList<>(elementsByTagName(tagName));
  }

  @JSBody(params = "tagName", script = "return document.getElementsByTagName(tagName);")
  private static native JSObject elementsByTagName(String tagName);

  public void setTitle(String title) {
    setDocumentTitle(title);
  }

  public String getTitle() {
    return getDocumentTitle();
  }

  @JSBody(params = "title", script = "document.title=title;")
  private static native void setDocumentTitle(String title);

  @JSBody(script = "return document.title;")
  private static native String getDocumentTitle();

  public VideoElement createVideoElement() {
    return VideoElement.as(createElement("video"));
  }

  public MetaElement createMetaElement() {
    return MetaElement.as(createElement("meta"));
  }

  public Element createElement(final String tagName) {
    return new Element(HTMLDocument.current().createElement(tagName));
  }

  public DivElement createDivElement() {
    return DivElement.as(createElement("div"));
  }

  public SpanElement createSpanElement() {
    return SpanElement.as(createElement("span"));
  }

  public Element createHElement(final int size) {
    return createElement("h" + size);
  }

  public Element createPushButtonElement() {
    return createElement("button");
  }

  public AnchorElement createAnchorElement() {
    return AnchorElement.as(createElement("a"));
  }

  public Element createBRElement() {
    return createElement("br");
  }

  public Element createDLElement() {
    return createElement("dl");
  }

  public Element createFieldSetElement() {
    return createElement("fieldset");
  }

  public ImageElement createImageElement() {
    return ImageElement.as(createElement("img"));
  }

  public LabelElement createLabelElement() {
    return LabelElement.as(createElement("label"));
  }

  public Element createLegendElement() {
    return createElement("legend");
  }

  public Element createLIElement() {
    return createElement("li");
  }

  public Element createOLElement() {
    return createElement("ol");
  }

  public Element createPreElement() {
    return createElement("pre");
  }

  public Element createULElement() {
    return createElement("ul");
  }

  public InputElement createInputElement(final String type) {
    final Element input = createElement("input");
    input.setAttribute("type", type);
    return InputElement.as(input);
  }

  public TextAreaElement createTextAreaElement() {
    return TextAreaElement.as(createElement("textarea"));
  }

  public OptGroupElement createOptGroupElement() {
    return OptGroupElement.as(createElement("optgroup"));
  }

  public LinkElement createLinkElement() {
    return LinkElement.as(createElement("link"));
  }

  public StyleElement createStyleElement() {
    return StyleElement.as(createElement("style"));
  }

  public SourceElement createSourceElement() {
    return SourceElement.as(createElement("source"));
  }

  public IFrameElement createIFrameElement() {
    return IFrameElement.as(createElement("iframe"));
  }

  public SelectElement createSelectElement() {
    return SelectElement.as(createElement("select"));
  }

  public OptionElement createOptionElement() {
    return OptionElement.as(createElement("option"));
  }

  public FormElement createFormElement() {
    return FormElement.as(createElement("form"));
  }

  public Element createPElement() {
    return createElement("p");
  }

  public Element createHRElement() {
    return createElement("hr");
  }

  public Element createTableElement() {
    return createElement("table");
  }

  public Element createNavElement() {
    return createElement("nav");
  }

  public Text createTextNode(final String data) {
    return new Text(HTMLDocument.current().createTextNode(data == null ? "" : data));
  }

  public InputElement createTextInputElement() {
    return createInputElement("text");
  }

  public InputElement createCheckInputElement() {
    return createInputElement("checkbox");
  }

  public InputElement createRadioInputElement(final String name) {
    final InputElement input = createInputElement("radio");
    input.setPropertyString("name", name);
    return input;
  }

  /** Returns an id unique within the document, matching GWT's {@code uid-N}. */
  public String createUniqueId() {
    return "uid-" + (++uniqueId);
  }

  private int uniqueId;

  public BodyElement getBody() {
    return new BodyElement(HTMLDocument.current().getBody());
  }

  public Element getElementById(final String id) {
    final HTMLElement found = HTMLDocument.current().getElementById(id);
    return found == null ? null : new Element(found);
  }

  /** Builds a synthetic click event, used to programmatically click a widget. */
  public NativeEvent createClickEvent(
      final int detail,
      final int screenX,
      final int screenY,
      final int clientX,
      final int clientY,
      final boolean ctrlKey,
      final boolean altKey,
      final boolean shiftKey,
      final boolean metaKey) {
    return createMouseEvent(
        "click", true, true, detail, screenX, screenY, clientX, clientY, ctrlKey, altKey, shiftKey,
        metaKey, 0, null);
  }

  public NativeEvent createMouseOverEvent(
      final int detail,
      final int screenX,
      final int screenY,
      final int clientX,
      final int clientY,
      final boolean ctrlKey,
      final boolean altKey,
      final boolean shiftKey,
      final boolean metaKey,
      final int button,
      final Element relatedTarget) {
    return createMouseEvent(
        "mouseover",
        true,
        true,
        detail,
        screenX,
        screenY,
        clientX,
        clientY,
        ctrlKey,
        altKey,
        shiftKey,
        metaKey,
        button,
        relatedTarget);
  }

  public NativeEvent createMouseOutEvent(
      final int detail,
      final int screenX,
      final int screenY,
      final int clientX,
      final int clientY,
      final boolean ctrlKey,
      final boolean altKey,
      final boolean shiftKey,
      final boolean metaKey,
      final int button,
      final Element relatedTarget) {
    return createMouseEvent(
        "mouseout",
        true,
        true,
        detail,
        screenX,
        screenY,
        clientX,
        clientY,
        ctrlKey,
        altKey,
        shiftKey,
        metaKey,
        button,
        relatedTarget);
  }

  public NativeEvent createMouseEvent(
      final String type,
      final boolean canBubble,
      final boolean cancelable,
      final int detail,
      final int screenX,
      final int screenY,
      final int clientX,
      final int clientY,
      final boolean ctrlKey,
      final boolean altKey,
      final boolean shiftKey,
      final boolean metaKey,
      final int button,
      final Element relatedTarget) {
    return new NativeEvent(
        newMouseEvent(
            type,
            canBubble,
            cancelable,
            detail,
            screenX,
            screenY,
            clientX,
            clientY,
            ctrlKey,
            altKey,
            shiftKey,
            metaKey,
            button,
            relatedTarget == null ? null : relatedTarget.unwrap()));
  }

  @org.teavm.jso.JSBody(
      params = {
        "type",
        "canBubble",
        "cancelable",
        "detail",
        "screenX",
        "screenY",
        "clientX",
        "clientY",
        "ctrlKey",
        "altKey",
        "shiftKey",
        "metaKey",
        "button",
        "relatedTarget"
      },
      script =
          "return new MouseEvent(type, {bubbles: canBubble, cancelable: cancelable,"
              + " detail: detail, screenX: screenX, screenY: screenY, clientX: clientX,"
              + " clientY: clientY, ctrlKey: ctrlKey, altKey: altKey, shiftKey: shiftKey,"
              + " metaKey: metaKey, button: button, relatedTarget: relatedTarget});")
  private static native org.teavm.jso.dom.events.Event newMouseEvent(
      String type,
      boolean canBubble,
      boolean cancelable,
      int detail,
      int screenX,
      int screenY,
      int clientX,
      int clientY,
      boolean ctrlKey,
      boolean altKey,
      boolean shiftKey,
      boolean metaKey,
      int button,
      HTMLElement relatedTarget);

  public Element createLabel() {
    return createElement("label");
  }

  public Element createDiv() {
    return createDivElement();
  }

  public Element createSpan() {
    return createSpanElement();
  }

  /** A synthetic {@code change} event, for programmatically driving a control. */
  public NativeEvent createKeyUpEvent(
      boolean ctrl, boolean alt, boolean shift, boolean meta, int keyCode) {
    return new NativeEvent(newKeyUpEvent(ctrl, alt, shift, meta, keyCode));
  }

  @org.teavm.jso.JSBody(
      params = {"ctrl", "alt", "shift", "meta", "keyCode"},
      script =
          "var event = new KeyboardEvent('keyup', {bubbles:true, cancelable:true, ctrlKey:ctrl, altKey:alt, shiftKey:shift, metaKey:meta});"
              + "Object.defineProperty(event, 'keyCode', {value:keyCode}); Object.defineProperty(event, 'which', {value:keyCode}); return event;")
  private static native org.teavm.jso.dom.events.Event newKeyUpEvent(
      boolean ctrl, boolean alt, boolean shift, boolean meta, int keyCode);

  public NativeEvent createChangeEvent() {

    return new NativeEvent(newEvent("change"));
  }

  public NativeEvent createBlurEvent() {
    return new NativeEvent(newEvent("blur"));
  }

  public NativeEvent createFocusEvent() {
    return new NativeEvent(newEvent("focus"));
  }

  @org.teavm.jso.JSBody(
      params = {"type"},
      script = "return new Event(type, {bubbles: true, cancelable: true});")
  private static native org.teavm.jso.dom.events.Event newEvent(String type);

  public Element createSubmitButtonElement() {
    final Element button = createElement("button");
    button.setAttribute("type", "submit");
    return button;
  }

  public Element createResetButtonElement() {
    final Element button = createElement("button");
    button.setAttribute("type", "reset");
    return button;
  }

  public Element createBlockQuoteElement() {
    return createElement("blockquote");
  }

  public Element createULElement2() {
    return createElement("ul");
  }

  public HeadElement getHead() {
    return new HeadElement(HTMLDocument.current().getHead());
  }

  public Element getDocumentElement() {
    return new Element(HTMLDocument.current().getDocumentElement());
  }

  public TableSectionElement createTFootElement() {
    return TableSectionElement.as(createElement("tfoot"));
  }
}
