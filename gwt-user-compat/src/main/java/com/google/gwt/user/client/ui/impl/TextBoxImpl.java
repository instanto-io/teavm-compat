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
package com.google.gwt.user.client.ui.impl;

import com.google.gwt.dom.client.Element;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.html.HTMLElement;

/**
 * Selection and cursor access for text inputs and textareas. GWT hides these behind a
 * deferred-binding impl for old-IE; on TeaVM one standards-based implementation does.
 */
public class TextBoxImpl {

  public int getCursorPos(final Element element) {
    return selectionStart(element.unwrap());
  }

  public int getSelectionLength(final Element element) {
    return selectionEnd(element.unwrap()) - selectionStart(element.unwrap());
  }

  public int getTextAreaCursorPos(final Element element) {
    return getCursorPos(element);
  }

  public int getTextAreaSelectionLength(final Element element) {
    return getSelectionLength(element);
  }

  public void setSelectionRange(final Element element, final int pos, final int length) {
    select(element.unwrap(), pos, pos + length);
  }

  @JSBody(
      params = {"el"},
      script = "return el.selectionStart == null ? 0 : el.selectionStart;")
  private static native int selectionStart(HTMLElement el);

  @JSBody(
      params = {"el"},
      script = "return el.selectionEnd == null ? 0 : el.selectionEnd;")
  private static native int selectionEnd(HTMLElement el);

  @JSBody(
      params = {"el", "start", "end"},
      script = "el.setSelectionRange(start, end);")
  private static native void select(HTMLElement el, int start, int end);
}
