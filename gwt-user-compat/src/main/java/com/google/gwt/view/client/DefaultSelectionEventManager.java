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
package com.google.gwt.view.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Turns cell events into selection changes, so a view can offer click, checkbox or blacklist-style
 * selection without each cell knowing about the selection model.
 *
 * <p>Install it with {@code display.addCellPreviewHandler(...)}.
 */
public class DefaultSelectionEventManager<T> implements CellPreviewEvent.Handler<T> {

  /** How a particular event should affect the selection. */
  public enum SelectAction {
    DEFAULT,
    SELECT,
    DESELECT,
    TOGGLE,
    IGNORE
  }

  /** Decides what an event means for the selection. */
  public interface EventTranslator<T> {

    /** True when this translator wants to clear the rest of the selection first. */
    boolean clearCurrentSelection(CellPreviewEvent<T> event);

    SelectAction translateSelectionEvent(CellPreviewEvent<T> event);
  }

  /** Treats a click anywhere in the row as a toggle. */
  public static <T> DefaultSelectionEventManager<T> createDefaultManager() {
    return new DefaultSelectionEventManager<>(null);
  }

  /** Selects only when the click lands on a checkbox in one of the given columns. */
  public static <T> DefaultSelectionEventManager<T> createCheckboxManager(final int... columns) {
    return new DefaultSelectionEventManager<>(new CheckboxEventTranslator<T>(columns));
  }

  /** Ignores events from the given columns and toggles on the rest. */
  public static <T> DefaultSelectionEventManager<T> createBlacklistManager(final int... columns) {
    return new DefaultSelectionEventManager<>(new BlacklistEventTranslator<T>(columns));
  }

  /** Toggles only on the given columns and ignores the rest. */
  public static <T> DefaultSelectionEventManager<T> createWhitelistManager(final int... columns) {
    return new DefaultSelectionEventManager<>(new WhitelistEventTranslator<T>(columns));
  }

  public static <T> DefaultSelectionEventManager<T> createCustomManager(
      final EventTranslator<T> translator) {
    return new DefaultSelectionEventManager<>(translator);
  }

  /** Selects when a checkbox in one of the listed columns changes. */
  public static class CheckboxEventTranslator<T> implements EventTranslator<T> {

    private final Set<Integer> columns = new HashSet<>();

    public CheckboxEventTranslator(final int... columns) {
      for (final int column : columns) {
        this.columns.add(column);
      }
    }

    @Override
    public boolean clearCurrentSelection(final CellPreviewEvent<T> event) {
      return false;
    }

    @Override
    public SelectAction translateSelectionEvent(final CellPreviewEvent<T> event) {
      if (!columns.isEmpty() && !columns.contains(event.getColumn())) {
        return SelectAction.IGNORE;
      }
      final NativeEvent nativeEvent = event.getNativeEvent();
      if (!"click".equals(nativeEvent.getType())) {
        return SelectAction.IGNORE;
      }
      final Element target = Element.as(nativeEvent.getEventTarget());
      if (target == null || !"input".equalsIgnoreCase(target.getTagName())) {
        return SelectAction.IGNORE;
      }
      return InputElement.as(target).isChecked() ? SelectAction.SELECT : SelectAction.DESELECT;
    }
  }

  /** Ignores the listed columns. */
  public static class BlacklistEventTranslator<T> implements EventTranslator<T> {

    private final Set<Integer> columns = new HashSet<>();

    public BlacklistEventTranslator(final int... columns) {
      for (final int column : columns) {
        this.columns.add(column);
      }
    }

    public void setColumnBlacklisted(final int index, final boolean blacklisted) {
      if (blacklisted) {
        columns.add(index);
      } else {
        columns.remove(index);
      }
    }

    @Override
    public boolean clearCurrentSelection(final CellPreviewEvent<T> event) {
      return false;
    }

    @Override
    public SelectAction translateSelectionEvent(final CellPreviewEvent<T> event) {
      return columns.contains(event.getColumn()) ? SelectAction.IGNORE : SelectAction.DEFAULT;
    }
  }

  /** Acts only on the listed columns. */
  public static class WhitelistEventTranslator<T> implements EventTranslator<T> {

    private final Set<Integer> columns = new HashSet<>();

    public WhitelistEventTranslator(final int... columns) {
      for (final int column : columns) {
        this.columns.add(column);
      }
    }

    public void setColumnWhitelisted(final int index, final boolean whitelisted) {
      if (whitelisted) {
        columns.add(index);
      } else {
        columns.remove(index);
      }
    }

    @Override
    public boolean clearCurrentSelection(final CellPreviewEvent<T> event) {
      return false;
    }

    @Override
    public SelectAction translateSelectionEvent(final CellPreviewEvent<T> event) {
      return columns.contains(event.getColumn()) ? SelectAction.DEFAULT : SelectAction.IGNORE;
    }
  }

  private final EventTranslator<T> translator;

  protected DefaultSelectionEventManager(final EventTranslator<T> translator) {
    this.translator = translator;
  }

  @Override
  public void onCellPreview(final CellPreviewEvent<T> event) {
    SelectAction action =
        translator == null ? SelectAction.DEFAULT : translator.translateSelectionEvent(event);
    if (action == SelectAction.IGNORE) {
      event.setCanceled(true);
      return;
    }
    if (action == SelectAction.DEFAULT) {
      if (!"click".equals(event.getNativeEvent().getType())) {
        return;
      }
      action = SelectAction.TOGGLE;
    }

    final SelectionModel<? super T> model = event.getDisplay().getSelectionModel();
    if (model == null) {
      return;
    }
    final T value = event.getValue();
    if (translator != null
        && translator.clearCurrentSelection(event)
        && model instanceof MultiSelectionModel) {
      ((MultiSelectionModel<?>) model).clear();
    }

    switch (action) {
      case SELECT:
        model.setSelected(value, true);
        break;
      case DESELECT:
        model.setSelected(value, false);
        break;
      case TOGGLE:
        model.setSelected(value, !model.isSelected(value));
        break;
      default:
        break;
    }
    // the manager owns selection for this event; stop the view doing it again
    event.setCanceled(true);
  }
}
