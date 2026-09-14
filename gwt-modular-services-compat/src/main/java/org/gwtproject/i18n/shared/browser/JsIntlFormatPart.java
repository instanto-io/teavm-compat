/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gwtproject.i18n.shared.browser;

import org.teavm.jso.JSClass;
import org.teavm.jso.JSObject;

/** A single part returned by {@code Intl.DateTimeFormat.prototype.formatToParts()}. */
@JSClass(name = "globalThis.Object")
public class JsIntlFormatPart implements JSObject {

  /** The type of this part (e.g. {@code "year"}, {@code "month"}, {@code "literal"}). */
  public String type;

  /** The string value of this part in the formatted output. */
  public String value;
}
