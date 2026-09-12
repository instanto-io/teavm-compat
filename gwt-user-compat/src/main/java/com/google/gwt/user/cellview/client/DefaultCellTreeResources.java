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

import com.google.gwt.resources.client.ImageResource;

/**
 * Default {@link CellTree.Resources}: empty style names and no images, so the widget carries no GWT
 * styling of its own.
 */
public class DefaultCellTreeResources implements CellTree.Resources {

  private final CellTree.Style style = new EmptyStyle();

  @Override
  public ImageResource cellTreeClosedItem() {
    return null;
  }

  @Override
  public ImageResource cellTreeLoading() {
    return null;
  }

  @Override
  public ImageResource cellTreeOpenItem() {
    return null;
  }

  @Override
  public ImageResource cellTreeSelectedBackground() {
    return null;
  }

  @Override
  public CellTree.Style cellTreeStyle() {
    return style;
  }

  /** Style whose every name is empty. */
  private static final class EmptyStyle implements CellTree.Style {

    @Override
    public String cellTreeEmptyMessage() {
      return "";
    }

    @Override
    public String cellTreeItem() {
      return "";
    }

    @Override
    public String cellTreeItemImage() {
      return "";
    }

    @Override
    public String cellTreeItemImageValue() {
      return "";
    }

    @Override
    public String cellTreeItemValue() {
      return "";
    }

    @Override
    public String cellTreeKeyboardSelectedItem() {
      return "";
    }

    @Override
    public String cellTreeOpenItem() {
      return "";
    }

    @Override
    public String cellTreeSelectedItem() {
      return "";
    }

    @Override
    public String cellTreeTopItem() {
      return "";
    }

    @Override
    public String cellTreeWidget() {
      return "";
    }

    @Override
    public String getText() {
      return "";
    }

    @Override
    public String getName() {
      return "";
    }

    @Override
    public boolean ensureInjected() {
      return false;
    }
  }
}
