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
package com.google.gwt.cell.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Text that turns into an input when clicked, and commits on Enter or blur.
 *
 * <p>The pending edit is held as view data keyed by the row, so it survives the re-render that
 * follows a click elsewhere in the view.
 */
public class EditTextCell extends AbstractEditableCell<String, EditTextCell.ViewData> {

  /** The state of an edit in progress on one row. */
  public static class ViewData {

    private boolean editing;
    private String original;
    private String text;

    public ViewData(final String text) {
      this.original = text;
      this.text = text;
      this.editing = true;
    }

    public String getOriginal() {
      return original;
    }

    public String getText() {
      return text;
    }

    public boolean isEditing() {
      return editing;
    }

    public boolean isEditingAgain() {
      return editing;
    }

    public void setText(final String text) {
      this.text = text;
    }

    public void setEditing(final boolean editing) {
      this.editing = editing;
    }

    protected void setOriginal(final String original) {
      this.original = original;
    }
  }

  public EditTextCell() {
    super("click", "keyup", "keydown", "blur");
  }

  @Override
  public boolean isEditing(final Context context, final Element parent, final String value) {
    final ViewData data = getViewData(context.getKey());
    return data != null && data.isEditing();
  }

  @Override
  public void onBrowserEvent(
      final Context context,
      final Element parent,
      final String value,
      final NativeEvent event,
      final ValueUpdater<String> valueUpdater) {
    final Object key = context.getKey();
    ViewData data = getViewData(key);
    final String type = event.getType();

    if (data == null || !data.isEditing()) {
      if ("click".equals(type)) {
        data = new ViewData(value);
        setViewData(key, data);
        setValue(context, parent, value);
        focusInput(parent);
      }
      return;
    }

    final Element input = parent.getFirstChildElement();
    if (input == null) {
      return;
    }
    final String current = InputElement.as(input).getValue();

    if ("keydown".equals(type)) {
      final int keyCode = event.getKeyCode();
      if (keyCode == KeyCodes.KEY_ENTER) {
        commit(context, parent, data, current, valueUpdater);
      } else if (keyCode == KeyCodes.KEY_ESCAPE) {
        cancel(context, parent, data);
      }
    } else if ("blur".equals(type)) {
      commit(context, parent, data, current, valueUpdater);
    } else {
      data.setText(current);
    }
  }

  @Override
  public void render(final Context context, final String value, final SafeHtmlBuilder sb) {
    final ViewData data = getViewData(context.getKey());
    if (data != null && data.isEditing()) {
      final String shown = data.getText() == null ? "" : data.getText();
      sb.appendHtmlConstant(
          "<input type=\"text\" tabindex=\"-1\" value=\""
              + SafeHtmlUtils.htmlEscape(shown)
              + "\"/>");
    } else if (value != null) {
      sb.appendEscaped(value);
    }
  }

  /** Ends an edit, keeping the typed value and reporting it upward. */
  public void commit(
      final Context context,
      final Element parent,
      final ViewData data,
      final String text,
      final ValueUpdater<String> valueUpdater) {
    data.setText(text);
    data.setEditing(false);
    clearViewData(context.getKey());
    setValue(context, parent, text);
    if (valueUpdater != null) {
      valueUpdater.update(text);
    }
  }

  /** Ends an edit, discarding the typed value. */
  public void cancel(final Context context, final Element parent, final ViewData data) {
    final String original = data.getOriginal();
    data.setEditing(false);
    clearViewData(context.getKey());
    setValue(context, parent, original);
  }

  private static void focusInput(final Element parent) {
    final Element input = parent.getFirstChildElement();
    if (input != null) {
      input.focus();
    }
  }
}
