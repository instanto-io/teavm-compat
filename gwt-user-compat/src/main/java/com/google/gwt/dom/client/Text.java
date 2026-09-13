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
import org.teavm.jso.dom.xml.Node;

/**
 * A DOM text node.
 *
 * <p>TeaVM's {@code Text} extends {@code Node} rather than {@code CharacterData}, so the data
 * accessors go through the node's {@code data} property directly.
 */
public class Text extends com.google.gwt.dom.client.Node {

  private final org.teavm.jso.dom.xml.Text node;

  public Text(final org.teavm.jso.dom.xml.Text node) {
    this.node = node;
  }

  public org.teavm.jso.dom.xml.Text unwrap() {
    return node;
  }

  public String getData() {
    return nodeData(node);
  }

  public void setData(final String data) {
    setNodeData(node, data == null ? "" : data);
  }

  public void removeFromParent() {
    final Node parent = node.getParentNode();
    if (parent != null) {
      parent.removeChild(node);
    }
  }

  @JSBody(
      params = {"n"},
      script = "return n.data;")
  private static native String nodeData(org.teavm.jso.dom.xml.Text n);

  @JSBody(
      params = {"n", "v"},
      script = "n.data = v;")
  private static native void setNodeData(org.teavm.jso.dom.xml.Text n, String v);

  /**
   * Views this text node as an {@link Element}, so it can back a widget. A text node is not an
   * element, so element-only operations on the result are meaningless; the widget classes use it
   * only for attach and removal.
   */
  @SuppressWarnings("unchecked")
  public <T extends com.google.gwt.core.client.JavaScriptObject> T cast() {
    return (T) new Element(asElement(node));
  }

  @JSBody(
      params = {"n"},
      script = "return n;")
  private static native org.teavm.jso.dom.html.HTMLElement asElement(org.teavm.jso.dom.xml.Text n);
}
