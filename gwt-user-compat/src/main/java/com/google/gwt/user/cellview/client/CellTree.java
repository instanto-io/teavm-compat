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
package com.google.gwt.user.cellview.client;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A tree whose nodes are rendered by {@link Cell}s and whose children come from a {@link
 * TreeViewModel}.
 *
 * <p>Children are fetched when a node is first opened, through the node's own {@code NodeInfo}, and
 * the branch below a node is re-rendered when its data arrives. GWT animates open and close and
 * supports keyboard navigation; neither is reproduced here.
 */
public class CellTree extends AbstractCellTree {

  /** GWT's default number of children fetched per node. */
  public static final int DEFAULT_LIST_SIZE = 25;

  /** Style names a tree applies to its parts. */
  public interface Style extends com.google.gwt.resources.client.CssResource {
    String cellTreeEmptyMessage();

    String cellTreeItem();

    String cellTreeItemImage();

    String cellTreeItemImageValue();

    String cellTreeItemValue();

    String cellTreeKeyboardSelectedItem();

    String cellTreeOpenItem();

    String cellTreeSelectedItem();

    String cellTreeTopItem();

    String cellTreeWidget();
  }

  /** Images and styles a tree uses. */
  public interface Resources extends com.google.gwt.resources.client.ClientBundle {
    com.google.gwt.resources.client.ImageResource cellTreeClosedItem();

    com.google.gwt.resources.client.ImageResource cellTreeLoading();

    com.google.gwt.resources.client.ImageResource cellTreeOpenItem();

    com.google.gwt.resources.client.ImageResource cellTreeSelectedBackground();

    Style cellTreeStyle();
  }

  /** One rendered node, and its open state. */
  private final class Node implements TreeNode {

    private final Object value;
    private final Node parent;
    private final int index;
    private final Element element;
    private final List<Node> children = new ArrayList<>();
    private TreeViewModel.NodeInfo<?> nodeInfo;
    private ChildView<?> childView;
    private boolean open;
    private boolean destroyed;

    Node(final Object value, final Node parent, final int index, final Element element) {
      this.value = value;
      this.parent = parent;
      this.index = index;
      this.element = element;
    }

    private void checkActive() {
      if (destroyed) {
        throw new IllegalStateException("Tree node has been destroyed");
      }
    }

    @Override
    public int getChildCount() {
      checkActive();
      return children.size();
    }

    @Override
    public Object getChildValue(final int i) {
      checkActive();
      return children.get(i).value;
    }

    @Override
    public int getIndex() {
      checkActive();
      return index;
    }

    @Override
    public TreeNode getParent() {
      checkActive();
      return parent;
    }

    @Override
    public Object getValue() {
      checkActive();
      return value;
    }

    @Override
    public boolean isChildLeaf(final int i) {
      checkActive();
      return viewModel.isLeaf(children.get(i).value);
    }

    @Override
    public boolean isChildOpen(final int i) {
      checkActive();
      return children.get(i).open;
    }

    @Override
    public boolean isDestroyed() {
      return destroyed;
    }

    @Override
    public TreeNode setChildOpen(final int i, final boolean shouldOpen) {
      return setChildOpen(i, shouldOpen, true);
    }

    @Override
    public TreeNode setChildOpen(final int i, final boolean shouldOpen, final boolean fireEvents) {
      checkActive();
      final Node child = children.get(i);
      setOpen(child, shouldOpen);
      return child.open ? child : null;
    }
  }

  /**
   * The view a node's children are pushed into. It is a {@link HasData} so the model's data
   * provider can drive it exactly as it drives a list or table.
   */
  private final class ChildView<T> extends AbstractHasData<T> {

    private final Node owner;
    private final Cell<T> cell;

    ChildView(final Node owner, final Cell<T> cell, final ProvidesKey<T> keyProvider) {
      super(defaultNodeSize, keyProvider);
      this.owner = owner;
      this.cell = cell;
      setElement(Document.get().createDivElement());
    }

    Cell<T> cell() {
      return cell;
    }

    @Override
    protected void render() {
      if (!owner.destroyed && owner.childView == this) {
        renderChildren(owner, this, cell);
      }
    }
  }

  private final TreeViewModel viewModel;
  private final Element root;
  private final Node rootNode;
  private int defaultNodeSize = DEFAULT_LIST_SIZE;
  private boolean listenerBound;

  public CellTree(final TreeViewModel viewModel, final Object rootValue) {
    this(viewModel, rootValue, null);
  }

  public CellTree(
      final TreeViewModel viewModel, final Object rootValue, final Resources resources) {
    super(viewModel, rootValue);
    this.viewModel = viewModel;
    this.root = Document.get().createDivElement();
    setElement(root);
    setStyleName("cellTree");
    this.rootNode = new Node(rootValue, null, 0, root);
    setOpen(rootNode, true);
  }

  public TreeNode getRootTreeNode() {
    return rootNode;
  }

  public int getDefaultNodeSize() {
    return defaultNodeSize;
  }

  public void setDefaultNodeSize(final int defaultNodeSize) {
    this.defaultNodeSize = defaultNodeSize;
  }

  /** Style put on the element of an open node. */
  protected String getOpenItemStyle() {
    return "open";
  }

  protected String getItemStyle() {
    return "item";
  }

  /** Opens or closes a node, fetching its children the first time it opens. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void setOpen(final Node node, final boolean open) {
    if (node.destroyed) {
      throw new IllegalStateException("Tree node has been destroyed");
    }
    if (node.open == open) {
      return;
    }
    node.open = open;

    if (!open) {
      node.destroyed = true;
      if (node.parent != null) {
        node.parent.children.set(
            node.index, new Node(node.value, node.parent, node.index, node.element));
      }
      if (node.nodeInfo != null) {
        node.nodeInfo.unsetDataDisplay();
        node.nodeInfo = null;
      }
      node.childView = null;
      destroyChildren(node);
      renderNodeChildren(node);
      return;
    }

    final TreeViewModel.NodeInfo<?> info = viewModel.getNodeInfo(node.value);
    if (info == null) {
      node.open = false;
      return;
    }
    node.nodeInfo = info;
    final ChildView view = new ChildView(node, info.getCell(), info.getProvidesKey());
    if (info.getSelectionModel() != null) {
      view.setSelectionModel((SelectionModel) info.getSelectionModel());
    }
    node.childView = view;
    ((TreeViewModel.NodeInfo) info).setDataDisplay(view);
  }

  private void destroyChildren(final Node node) {
    for (final Node child : node.children) {
      child.destroyed = true;
      destroyChildren(child);
      if (child.nodeInfo != null) {
        child.nodeInfo.unsetDataDisplay();
        child.nodeInfo = null;
      }
      child.childView = null;
      child.open = false;
    }
    node.children.clear();
  }

  /** Rebuilds a node's child elements from the values its view now holds. */
  private <T> void renderChildren(final Node node, final ChildView<T> view, final Cell<T> cell) {
    destroyChildren(node);
    final List<T> values = view.getRowData();
    for (int i = 0; i < values.size(); i++) {
      final T value = values.get(i);
      final Element child = Document.get().createDivElement();
      child.addClassName(getItemStyle());
      final SafeHtmlBuilder sb = new SafeHtmlBuilder();
      cell.render(new Cell.Context(i, 0, value), value, sb);
      child.setInnerSafeHtml(sb.toSafeHtml());
      node.children.add(new Node(value, node, i, child));
    }
    renderNodeChildren(node);
    ensureListener();
  }

  /** Writes a node's children into the document under its own element. */
  private void renderNodeChildren(final Node node) {
    final Element host = node == rootNode ? root : node.element;
    // keep the node's own rendered value, drop any previously rendered children
    Element existing = host.getFirstChildElement();
    while (existing != null) {
      final Element next = existing.getNextSiblingElement();
      if (existing.hasClassName(getItemStyle())) {
        existing.removeFromParent();
      }
      existing = next;
    }
    if (!node.open) {
      host.removeClassName(getOpenItemStyle());
      return;
    }
    host.addClassName(getOpenItemStyle());
    for (final Node child : node.children) {
      host.appendChild(child.element);
    }
  }

  private void ensureListener() {
    if (listenerBound) {
      return;
    }
    listenerBound = true;
    root.unwrap().addEventListener("click", this::onClick);
  }

  /** A click on a node toggles it open, as GWT's disclosure affordance does. */
  private void onClick(final org.teavm.jso.dom.events.Event nativeEvent) {
    final NativeEvent event = new NativeEvent(nativeEvent);
    final Element target = Element.as(event.getEventTarget());
    if (target == null) {
      return;
    }
    final Node node = findNode(rootNode, target);
    if (node == null || node == rootNode) {
      return;
    }
    if (!viewModel.isLeaf(node.value)) {
      setOpen(node, !node.open);
    }
  }

  private Node findNode(final Node from, final Element target) {
    for (final Node child : from.children) {
      if (child.element.isOrHasChild(target)) {
        final Node deeper = findNode(child, target);
        return deeper == null ? child : deeper;
      }
    }
    return null;
  }
}
