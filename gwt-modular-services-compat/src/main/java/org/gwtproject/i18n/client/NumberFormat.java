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
package org.gwtproject.i18n.client;

import org.gwtproject.i18n.shared.browser.JsIntlNumberFormat;
import org.gwtproject.i18n.shared.browser.JsIntlNumberFormatOptions;
import org.gwtproject.i18n.shared.cldr.LocaleInfo;
import org.gwtproject.i18n.shared.cldr.NumberConstants;

/**
 * Stub of GWT's {@code NumberFormat} that delegates formatting to the browser's {@code
 * Intl.NumberFormat} API and parsing to {@link Double#parseDouble} after normalising
 * locale-specific separators.
 *
 * <p>Instances are stateless and safe to cache.
 */
public class NumberFormat {

  private final JsIntlNumberFormat intlFormat;
  private final NumberConstants numberConstants;
  private final boolean isDecimal;
  private boolean currency;

  private NumberFormat(JsIntlNumberFormat intlFormat, boolean isDecimal) {
    this.intlFormat = intlFormat;
    this.numberConstants = LocaleInfo.getCurrentLocale().getNumberConstants();
    this.isDecimal = isDecimal;
  }

  // ---- static factories ----

  /**
   * Returns a {@link NumberFormat} for the given GWT-style pattern. The pattern is used to set
   * fraction-digit constraints on the underlying {@code Intl.NumberFormat} instance; it is not
   * parsed as a full DecimalFormat pattern.
   */
  public static NumberFormat getFormat(String pattern) {
    JsIntlNumberFormatOptions opts = JsIntlNumberFormatOptions.decimal();
    int minFrac = countMinFractionDigits(pattern);
    int maxFrac = countMaxFractionDigits(pattern);
    if (minFrac >= 0) opts.minimumFractionDigits = minFrac;
    if (maxFrac >= 0) opts.maximumFractionDigits = maxFrac;
    return new NumberFormat(JsIntlNumberFormat.create(opts), false);
  }

  /** Returns a {@link NumberFormat} using the locale's default decimal format. */
  public static NumberFormat getDecimalFormat() {
    return new NumberFormat(JsIntlNumberFormat.decimal(), true);
  }

  /** Formats the requested currency using the browser's locale and currency precision. */
  public static NumberFormat getCurrencyFormat(String currencyCode) {
    if (currencyCode == null || !currencyCode.matches("[A-Za-z]{3}")) {
      throw new IllegalArgumentException("Expected a three-letter currency code");
    }
    JsIntlNumberFormatOptions options = JsIntlNumberFormatOptions.create();
    options.style = "currency";
    options.currency = currencyCode.toUpperCase(java.util.Locale.ROOT);
    NumberFormat result = new NumberFormat(JsIntlNumberFormat.create(options), false);
    result.currency = true;
    return result;
  }

  // ---- formatting ----

  /** Formats a {@code double} value. */
  public String format(double value) {
    return intlFormat.format(value);
  }

  /** Formats a {@link Number} value. */
  public String format(Number value) {
    return format(value.doubleValue());
  }

  // ---- parsing ----

  /**
   * Parses a localised number string. The locale's decimal separator and grouping separator are
   * normalised before parsing so that {@link Double#parseDouble} can handle the result.
   *
   * @throws NumberFormatException if the string cannot be parsed.
   */
  public double parse(String text) {
    if (text == null || text.trim().isEmpty()) {
      throw new NumberFormatException("Cannot parse empty number string");
    }
    String normalised = normalise(text);
    try {
      return Double.parseDouble(normalised);
    } catch (NumberFormatException e) {
      throw new NumberFormatException("Cannot parse number: " + text);
    }
  }

  // ---- private helpers ----

  private String normalise(String text) {
    String decSep = numberConstants.decimalSeparator();
    String grpSep = numberConstants.groupingSeparator();
    String minusSign = numberConstants.minusSign();

    String result = text.trim();
    if (currency) {
      // Currency symbols and spacing come from the same formatter as format().
      // Do not strip arbitrary letters: malformed input must still fail parsing.
      var parts = intlFormat.formatToParts(-12345.6);
      for (int i = 0; i < parts.length; i++) {
        var part = parts.getAt(i);
        if ("currency".equals(part.type) || "literal".equals(part.type)) {
          result = result.replace(part.value, "");
        }
      }
    }
    // Replace locale minus sign with ASCII hyphen-minus
    if (!"-".equals(minusSign)) {
      result = result.replace(minusSign, "-");
    }
    // Remove grouping separators
    if (!grpSep.isEmpty()) {
      result = result.replace(grpSep, "");
    }
    // Replace locale decimal separator with ASCII dot
    if (!".".equals(decSep)) {
      result = result.replace(decSep, ".");
    }
    return result;
  }

  /** Counts minimum fraction digits in a GWT-style decimal pattern (trailing zeros after dot). */
  private static int countMinFractionDigits(String pattern) {
    int dot = pattern.indexOf('.');
    if (dot < 0) return 0;
    int count = 0;
    for (int i = dot + 1; i < pattern.length(); i++) {
      if (pattern.charAt(i) == '0') count++;
      else break;
    }
    return count;
  }

  /** Counts maximum fraction digits in a GWT-style decimal pattern (# and 0 after dot). */
  private static int countMaxFractionDigits(String pattern) {
    int dot = pattern.indexOf('.');
    if (dot < 0) return 0;
    int count = 0;
    for (int i = dot + 1; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      if (c == '0' || c == '#') count++;
      else break;
    }
    return count;
  }
}
