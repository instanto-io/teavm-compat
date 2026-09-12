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
package com.google.gwt.user.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/** A text box with a dropdown of completions supplied by a {@link SuggestOracle}. */
public class SuggestBox extends Composite implements HasValue<String>, HasEnabled {

  /** Receives the suggestion a user picked. */
  public interface SuggestionCallback {
    void onSuggestionSelected(SuggestOracle.Suggestion suggestion);
  }

  /** Renders the suggestion list and reports the user's choice. */
  public static class SuggestionDisplay {

    private final FlowPanel suggestionItems = new FlowPanel();
    private final PopupPanel popup = new PopupPanel(true);
    private SuggestBox owner;

    protected SuggestionDisplay() {
      popup.setWidget(suggestionItems);
      popup.setStyleName("dropdown-menu");
      popup.getElement().getStyle().setProperty("position", "absolute");
      hideSuggestions();
    }

    void attachTo(final SuggestBox box) {
      owner = box;
    }

    Widget asPopup() {
      return popup;
    }

    protected void showSuggestions(
        final SuggestBox suggestBox,
        final java.util.Collection<? extends SuggestOracle.Suggestion> suggestions,
        final boolean isDisplayStringHTML,
        final boolean isAutoSelectEnabled,
        final SuggestionCallback callback) {
      suggestionItems.clear();
      if (suggestions.isEmpty()) {
        hideSuggestions();
        return;
      }
      for (final SuggestOracle.Suggestion suggestion : suggestions) {
        final Anchor item = new Anchor(suggestion.getDisplayString());
        item.addStyleName("dropdown-item");
        item.setHref("javascript:;");
        item.addClickHandler(
            event -> {
              event.preventDefault();
              callback.onSuggestionSelected(suggestion);
            });
        suggestionItems.add(item);
      }
      popup.addStyleName("show");
      popup.setVisible(true);
    }

    protected void hideSuggestions() {
      popup.removeStyleName("show");
      popup.setVisible(false);
    }

    public boolean isSuggestionListShowing() {
      return popup.isVisible();
    }

    /** The panel the suggestions are rendered into. */
    protected Widget getPopupPanel() {
      return popup;
    }

    protected void setPositionRelativeTo(final UIObject target) {}
  }

  /** The display GWT uses by default: a popup list beneath the text box. */
  public static class DefaultSuggestionDisplay extends SuggestionDisplay {

    private boolean animationEnabled;

    public boolean isAnimationEnabled() {
      return animationEnabled;
    }

    public void setAnimationEnabled(final boolean enable) {
      animationEnabled = enable;
    }

    /** GWT exposes the popup so callers can restyle or reposition it. */
    @Override
    public PopupPanel getPopupPanel() {
      return (PopupPanel) super.getPopupPanel();
    }

    public void hideSuggestions() {
      super.hideSuggestions();
    }
  }

  private final FlowPanel container = new FlowPanel();
  private final ValueBoxBase<String> box;
  private final SuggestionDisplay display;
  private SuggestOracle oracle;
  private int limit = 20;

  public SuggestBox() {
    this(new MultiWordSuggestOracle());
  }

  public SuggestBox(final SuggestOracle oracle) {
    this(oracle, new TextBox());
  }

  public SuggestBox(final SuggestOracle oracle, final ValueBoxBase<String> box) {
    this(oracle, box, new SuggestionDisplay());
  }

  public SuggestBox(
      final SuggestOracle oracle, final ValueBoxBase<String> box, final SuggestionDisplay display) {
    this.oracle = oracle;
    this.box = box;
    this.display = display;
    display.attachTo(this);
    container.getElement().getStyle().setProperty("position", "relative");
    container.add(box);
    container.add(display.asPopup());
    initWidget(container);
    box.addKeyUpHandler(event -> refreshSuggestions());
  }

  private void refreshSuggestions() {
    final String query = box.getValueAsString();
    oracle.requestSuggestions(
        new SuggestOracle.Request(query, limit),
        (request, response) -> {
          display.showSuggestions(
              this,
              response.getSuggestions(),
              oracle.isDisplayStringHTML(),
              true,
              this::applySuggestion);
        });
  }

  public void applySuggestion(final SuggestOracle.Suggestion suggestion) {
    setValue(suggestion.getReplacementString(), true);
    display.hideSuggestions();
  }

  /**
   * The value box this SuggestBox wraps.
   *
   * <p>Real GWT declares both this and {@code getTextBox()}; the latter casts to {@code
   * TextBoxBase}, which the Bootstrap TextBox does not extend, so this is the accessor the widget
   * library uses.
   */
  public ValueBoxBase<String> getValueBox() {
    return box;
  }

  public ValueBoxBase<String> getTextBox() {
    return box;
  }

  @Override
  public boolean isEnabled() {
    return box.isEnabled();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    box.setEnabled(enabled);
  }

  public SuggestOracle getSuggestOracle() {
    return oracle;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(final int limit) {
    this.limit = limit;
  }

  public String getText() {
    return box.getValueAsString();
  }

  public void setText(final String text) {
    box.setValue(text);
  }

  @Override
  public String getValue() {
    return box.getValueAsString();
  }

  @Override
  public void setValue(final String value) {
    setValue(value, false);
  }

  @Override
  public void setValue(final String value, final boolean fireEvents) {
    final String oldValue = getValue();
    box.setValue(value);
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldValue, getValue());
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.<String>getType());
  }
}
