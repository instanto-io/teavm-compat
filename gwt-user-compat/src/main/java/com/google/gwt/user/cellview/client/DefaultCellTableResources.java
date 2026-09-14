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
 * Default {@link CellTable.Resources}.
 *
 * <p>GWT generates these from a CSS bundle. Every style name here is empty and every image null, so
 * the table carries no GWT styling of its own -- which is what a Bootstrap-styled table wants, and
 * what the Bootstrap widgets' own resource adapters go on to enforce.
 */
public class DefaultCellTableResources implements CellTable.Resources {

  private final CellTable.Style style = new EmptyStyle();

  @Override
  public ImageResource cellTableFooterBackground() {
    return null;
  }

  @Override
  public ImageResource cellTableHeaderBackground() {
    return null;
  }

  @Override
  public ImageResource cellTableLoading() {
    return null;
  }

  @Override
  public ImageResource cellTableSelectedBackground() {
    return null;
  }

  @Override
  public ImageResource cellTableSortAscending() {
    return null;
  }

  @Override
  public ImageResource cellTableSortDescending() {
    return null;
  }

  @Override
  public CellTable.Style cellTableStyle() {
    return style;
  }

  /** Style whose every name is empty. */
  private static final class EmptyStyle implements CellTable.Style {

    @Override
    public String cellTableCell() {
      return "";
    }

    @Override
    public String cellTableEvenRow() {
      return "";
    }

    @Override
    public String cellTableEvenRowCell() {
      return "";
    }

    @Override
    public String cellTableFirstColumn() {
      return "";
    }

    @Override
    public String cellTableFirstColumnFooter() {
      return "";
    }

    @Override
    public String cellTableFirstColumnHeader() {
      return "";
    }

    @Override
    public String cellTableFooter() {
      return "";
    }

    @Override
    public String cellTableHeader() {
      return "";
    }

    @Override
    public String cellTableHoveredRow() {
      return "";
    }

    @Override
    public String cellTableHoveredRowCell() {
      return "";
    }

    @Override
    public String cellTableKeyboardSelectedCell() {
      return "";
    }

    @Override
    public String cellTableKeyboardSelectedRow() {
      return "";
    }

    @Override
    public String cellTableKeyboardSelectedRowCell() {
      return "";
    }

    @Override
    public String cellTableLastColumn() {
      return "";
    }

    @Override
    public String cellTableLastColumnFooter() {
      return "";
    }

    @Override
    public String cellTableLastColumnHeader() {
      return "";
    }

    @Override
    public String cellTableLoading() {
      return "";
    }

    @Override
    public String cellTableOddRow() {
      return "";
    }

    @Override
    public String cellTableOddRowCell() {
      return "";
    }

    @Override
    public String cellTableSelectedRow() {
      return "";
    }

    @Override
    public String cellTableSelectedRowCell() {
      return "";
    }

    @Override
    public String cellTableSortableHeader() {
      return "";
    }

    @Override
    public String cellTableSortedHeaderAscending() {
      return "";
    }

    @Override
    public String cellTableSortedHeaderDescending() {
      return "";
    }

    @Override
    public String cellTableWidget() {
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
