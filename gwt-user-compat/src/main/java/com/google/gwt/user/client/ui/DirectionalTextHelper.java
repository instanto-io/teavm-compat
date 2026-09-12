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

import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.DirectionEstimator;

/**
 * Sets text or HTML on an element, tracking the writing direction that goes with it.
 *
 * <p>Direction estimation is a no-op here: the widgets use this class as their text accessor, and
 * carry the direction API through, but the page's own {@code dir} handling governs rendering.
 */
public class DirectionalTextHelper {

  private final Element element;
  private final boolean isElementInline;
  private Direction textDirection = Direction.DEFAULT;
  private DirectionEstimator directionEstimator;
  private boolean isDirectionExplicitlySet;

  public DirectionalTextHelper(final Element element, final boolean isElementInline) {
    this.element = element;
    this.isElementInline = isElementInline;
  }

  public DirectionEstimator getDirectionEstimator() {
    return directionEstimator;
  }

  public void setDirectionEstimator(final DirectionEstimator directionEstimator) {
    this.directionEstimator = directionEstimator;
  }

  public void setDirectionEstimator(final boolean enabled) {
    this.directionEstimator = enabled ? new DirectionEstimator() {} : null;
  }

  public Direction getTextDirection() {
    return textDirection;
  }

  public String getTextOrHtml(final boolean isHtml) {
    return isHtml ? element.getInnerHTML() : element.getInnerText();
  }

  public void setTextOrHtml(final String content, final boolean isHtml) {
    if (directionEstimator != null && !isDirectionExplicitlySet) {
      textDirection = directionEstimator.estimateDirection(content, isHtml);
    }
    write(content, isHtml);
  }

  public void setTextOrHtml(final String content, final Direction direction, final boolean isHtml) {
    textDirection = direction == null ? Direction.DEFAULT : direction;
    isDirectionExplicitlySet = true;
    applyDirection();
    write(content, isHtml);
  }

  private void write(final String content, final boolean isHtml) {
    if (isHtml) {
      element.setInnerHTML(content == null ? "" : content);
    } else {
      element.setInnerText(content == null ? "" : content);
    }
  }

  private void applyDirection() {
    switch (textDirection) {
      case RTL:
        element.setAttribute("dir", "rtl");
        break;
      case LTR:
        element.setAttribute("dir", "ltr");
        break;
      default:
        element.removeAttribute("dir");
        break;
    }
  }
}
