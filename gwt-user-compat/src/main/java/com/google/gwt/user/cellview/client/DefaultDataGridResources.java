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
 * Default {@link DataGrid.Resources}.
 *
 * <p>GWT generates these from a CSS bundle. Every style name here is empty and every image null, so
 * the table carries no GWT styling of its own -- which is what a Bootstrap-styled table wants, and
 * what the Bootstrap widgets' own resource adapters go on to enforce.
 */
public class DefaultDataGridResources implements DataGrid.Resources {

  private final DataGrid.Style style = new EmptyStyle();

  @Override
  public ImageResource dataGridLoading() {
    return null;
  }

  @Override
  public ImageResource dataGridSortAscending() {
    return null;
  }

  @Override
  public ImageResource dataGridSortDescending() {
    return null;
  }

  @Override
  public DataGrid.Style dataGridStyle() {
    return style;
  }

  /** Style whose every name is empty. */
  private static final class EmptyStyle implements DataGrid.Style {

    @Override
    public String dataGridCell() {
      return "";
    }

    @Override
    public String dataGridEvenRow() {
      return "";
    }

    @Override
    public String dataGridEvenRowCell() {
      return "";
    }

    @Override
    public String dataGridFirstColumn() {
      return "";
    }

    @Override
    public String dataGridFirstColumnFooter() {
      return "";
    }

    @Override
    public String dataGridFirstColumnHeader() {
      return "";
    }

    @Override
    public String dataGridFooter() {
      return "";
    }

    @Override
    public String dataGridHeader() {
      return "";
    }

    @Override
    public String dataGridHoveredRow() {
      return "";
    }

    @Override
    public String dataGridHoveredRowCell() {
      return "";
    }

    @Override
    public String dataGridKeyboardSelectedCell() {
      return "";
    }

    @Override
    public String dataGridKeyboardSelectedRow() {
      return "";
    }

    @Override
    public String dataGridKeyboardSelectedRowCell() {
      return "";
    }

    @Override
    public String dataGridLastColumn() {
      return "";
    }

    @Override
    public String dataGridLastColumnFooter() {
      return "";
    }

    @Override
    public String dataGridLastColumnHeader() {
      return "";
    }

    @Override
    public String dataGridOddRow() {
      return "";
    }

    @Override
    public String dataGridOddRowCell() {
      return "";
    }

    @Override
    public String dataGridSelectedRow() {
      return "";
    }

    @Override
    public String dataGridSelectedRowCell() {
      return "";
    }

    @Override
    public String dataGridSortableHeader() {
      return "";
    }

    @Override
    public String dataGridSortedHeaderAscending() {
      return "";
    }

    @Override
    public String dataGridSortedHeaderDescending() {
      return "";
    }

    @Override
    public String dataGridWidget() {
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
