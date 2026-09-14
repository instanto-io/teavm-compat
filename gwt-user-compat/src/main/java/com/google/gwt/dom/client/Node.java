package com.google.gwt.dom.client;

/** Common typed view for element and text nodes. */
public abstract class Node extends com.google.gwt.core.client.JavaScriptObject {
  protected Node() {
    super(null);
  }

  public abstract org.teavm.jso.dom.xml.Node unwrap();

  public <T extends Node> T appendChild(T child) {
    unwrap().appendChild(child.unwrap());
    return child;
  }

  public <T extends Node> T insertBefore(T child, Node before) {
    unwrap().insertBefore(child.unwrap(), before == null ? null : before.unwrap());
    return child;
  }

  public int getChildCount() {
    return unwrap().getChildNodes().getLength();
  }

  public Node getChild(int index) {
    return wrap(unwrap().getChildNodes().item(index));
  }

  public Node getFirstChild() {
    return wrap(unwrap().getFirstChild());
  }

  public void removeFromParent() {
    org.teavm.jso.dom.xml.Node node = unwrap();
    if (node.getParentNode() != null) node.getParentNode().removeChild(node);
  }

  // DOM constructors belong to each frame's realm; use structural nodeType checks above
  // the native view instead of instanceof the host window's HTMLElement constructor.
  @org.teavm.jso.JSBody(params = "node", script = "return node;")
  private static native org.teavm.jso.dom.html.HTMLElement asElement(
      org.teavm.jso.dom.xml.Node node);

  @org.teavm.jso.JSBody(params = "node", script = "return node;")
  private static native org.teavm.jso.dom.xml.Text asText(org.teavm.jso.dom.xml.Node node);

  public static Node wrap(org.teavm.jso.dom.xml.Node node) {
    if (node == null) return null;
    if (node.getNodeType() == 1) return Element.wrap(asElement(node));
    if (node.getNodeType() == 3) return new Text(asText(node));
    return new Node() {
      @Override
      public org.teavm.jso.dom.xml.Node unwrap() {
        return node;
      }
    };
  }
}
