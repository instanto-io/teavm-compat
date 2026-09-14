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
package com.google.gwt.user.client;

import org.teavm.jso.browser.Window;

/** One-shot and repeating timer, matching GWT's Timer surface. */
public abstract class Timer {

  private int handle = -1;
  private boolean repeating;

  public abstract void run();

  public void schedule(final int delayMillis) {
    cancel();
    repeating = false;
    handle = Window.setTimeout(this::fire, delayMillis);
  }

  public void scheduleRepeating(final int periodMillis) {
    cancel();
    repeating = true;
    handle = Window.setInterval(this::run, periodMillis);
  }

  public void cancel() {
    if (handle == -1) {
      return;
    }
    if (repeating) {
      Window.clearInterval(handle);
    } else {
      Window.clearTimeout(handle);
    }
    handle = -1;
  }

  private void fire() {
    handle = -1;
    run();
  }
}
