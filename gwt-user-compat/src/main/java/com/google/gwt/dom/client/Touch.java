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
package com.google.gwt.dom.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.teavm.jso.*;

public class Touch extends JavaScriptObject {
  public Touch(JSObject value) {
    super(value);
  }

  public int getClientX() {
    return integer(unwrap(), "clientX");
  }

  public int getClientY() {
    return integer(unwrap(), "clientY");
  }

  public int getIdentifier() {
    return integer(unwrap(), "identifier");
  }

  public int getPageX() {
    return integer(unwrap(), "pageX");
  }

  public int getPageY() {
    return integer(unwrap(), "pageY");
  }

  public int getScreenX() {
    return integer(unwrap(), "screenX");
  }

  public int getScreenY() {
    return integer(unwrap(), "screenY");
  }

  public EventTarget getTarget() {
    return new EventTarget(target(unwrap()));
  }

  public int getRelativeX(Element target) {
    return getClientX()
        - target.getAbsoluteLeft()
        + target.getScrollLeft()
        + scroll(target.unwrap(), true);
  }

  public int getRelativeY(Element target) {
    return getClientY()
        - target.getAbsoluteTop()
        + target.getScrollTop()
        + scroll(target.unwrap(), false);
  }

  @JSBody(
      params = {"t", "name"},
      script = "return t[name] | 0;")
  private static native int integer(JSObject touch, String name);

  @JSBody(params = "t", script = "return t.target;")
  private static native org.teavm.jso.dom.events.EventTarget target(JSObject touch);

  @JSBody(
      params = {"e", "x"},
      script =
          "return (x ? e.ownerDocument.defaultView.pageXOffset : e.ownerDocument.defaultView.pageYOffset) | 0;")
  private static native int scroll(org.teavm.jso.dom.html.HTMLElement e, boolean x);
}
