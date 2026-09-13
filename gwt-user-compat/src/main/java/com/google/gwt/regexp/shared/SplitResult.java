package com.google.gwt.regexp.shared;

/** Result of splitting a string with a regular expression. */
public final class SplitResult {
  private final String[] values;

  public SplitResult(String[] values) {
    this.values = values;
  }

  public int length() {
    return values.length;
  }

  public String get(int index) {
    return values[index];
  }

  public void set(int index, String value) {
    values[index] = value;
  }
}
