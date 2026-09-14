package com.google.gwt.i18n.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Locale;

/** Default-locale GWT number patterns, implemented by TeaVM's decimal formatter. */
public class NumberFormat {
  private final DecimalFormat delegate;
  private final String pattern;

  protected NumberFormat(String pattern) {
    this.pattern = pattern;
    delegate = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US));
  }

  public static NumberFormat getFormat(String pattern) {
    return new NumberFormat(pattern);
  }

  public static NumberFormat getDecimalFormat() {
    return getFormat("#,##0.###");
  }

  public static NumberFormat getPercentFormat() {
    return getFormat("#,##0%");
  }

  public static NumberFormat getScientificFormat() {
    return getFormat("0E0");
  }

  public static NumberFormat getCurrencyFormat() {
    return getFormat("¤#,##0.00;(¤#,##0.00)");
  }

  public String format(double value) {
    return delegate.format(value);
  }

  public String format(Number value) {
    return delegate.format(value);
  }

  public String getPattern() {
    return pattern;
  }

  public NumberFormat overrideFractionDigits(int digits) {
    return overrideFractionDigits(digits, digits);
  }

  public NumberFormat overrideFractionDigits(int minimum, int maximum) {
    if (minimum < 0 || maximum < minimum)
      throw new IllegalArgumentException("Invalid fraction digits");
    NumberFormat result = new NumberFormat(pattern);
    result.delegate.setMinimumFractionDigits(minimum);
    result.delegate.setMaximumFractionDigits(maximum);
    return result;
  }

  public double parse(String text) {
    int[] cursor = {0};
    double value = parse(text, cursor);
    if (cursor[0] != text.length()) throw new NumberFormatException(text);
    return value;
  }

  public double parse(String text, int[] position) {
    ParsePosition cursor = new ParsePosition(position[0]);
    Number value = delegate.parse(text, cursor);
    if (value == null) throw new NumberFormatException(text);
    // TeaVM's DecimalFormat parser leaves the literal suffix at the cursor.
    String suffix =
        value.doubleValue() < 0 ? delegate.getNegativeSuffix() : delegate.getPositiveSuffix();
    if (suffix != null
        && !suffix.isEmpty()
        && text.startsWith(suffix, cursor.getIndex())
        && !text.substring(position[0], cursor.getIndex()).endsWith(suffix)) {
      cursor.setIndex(cursor.getIndex() + suffix.length());
    }
    position[0] = cursor.getIndex();
    // GWT consumes a pattern's percent suffix without applying its formatting multiplier.
    return value.doubleValue() * delegate.getMultiplier();
  }
}
