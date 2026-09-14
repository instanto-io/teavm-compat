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
package com.google.gwt.i18n.shared;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Date formatting, backed by {@link SimpleDateFormat}.
 *
 * <p>GWT compiles its patterns against CLDR data at build time; here the pattern is handed to the
 * platform formatter, which TeaVM implements over its own CLDR data. The pattern syntax is the
 * same, so a format string written for GWT behaves the same.
 */
public class DateTimeFormat {

  /** The predefined formats GWT offers. */
  public enum PredefinedFormat {
    ISO_8601("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
    RFC_2822("EEE, d MMM yyyy HH:mm:ss Z"),
    DATE_FULL("EEEE, d MMMM yyyy"),
    DATE_LONG("d MMMM yyyy"),
    DATE_MEDIUM("d MMM yyyy"),
    DATE_SHORT("dd/MM/yy"),
    TIME_FULL("HH:mm:ss zzzz"),
    TIME_LONG("HH:mm:ss z"),
    TIME_MEDIUM("HH:mm:ss"),
    TIME_SHORT("HH:mm"),
    DATE_TIME_FULL("EEEE, d MMMM yyyy HH:mm:ss zzzz"),
    DATE_TIME_LONG("d MMMM yyyy HH:mm:ss z"),
    DATE_TIME_MEDIUM("d MMM yyyy HH:mm:ss"),
    DATE_TIME_SHORT("dd/MM/yy HH:mm"),
    DAY("d"),
    HOUR_MINUTE("HH:mm"),
    HOUR_MINUTE_SECOND("HH:mm:ss"),
    HOUR24_MINUTE("HH:mm"),
    HOUR24_MINUTE_SECOND("HH:mm:ss"),
    MINUTE_SECOND("mm:ss"),
    MONTH("MMMM"),
    MONTH_ABBR("MMM"),
    MONTH_ABBR_DAY("MMM d"),
    MONTH_DAY("MMMM d"),
    MONTH_NUM_DAY("M/d"),
    MONTH_WEEKDAY_DAY("MMMM EEEE d"),
    YEAR("yyyy"),
    YEAR_MONTH("yyyy MMMM"),
    YEAR_MONTH_ABBR("yyyy MMM"),
    YEAR_MONTH_ABBR_DAY("d MMM yyyy"),
    YEAR_MONTH_DAY("d MMMM yyyy"),
    YEAR_MONTH_NUM("M/yyyy"),
    YEAR_MONTH_NUM_DAY("d/M/yyyy"),
    YEAR_MONTH_WEEKDAY_DAY("EEE, d MMM yyyy"),
    YEAR_QUARTER("yyyy QQQQ"),
    YEAR_QUARTER_ABBR("yyyy QQQ");

    private final String pattern;

    PredefinedFormat(final String pattern) {
      this.pattern = pattern;
    }

    public String getPattern() {
      return pattern;
    }
  }

  private static final Map<String, DateTimeFormat> CACHE = new HashMap<>();

  private final String pattern;
  private final SimpleDateFormat format;

  protected DateTimeFormat(final String pattern) {
    this.pattern = pattern;
    this.format = new SimpleDateFormat(pattern);
  }

  public static DateTimeFormat getFormat(final String pattern) {
    return CACHE.computeIfAbsent(pattern, DateTimeFormat::new);
  }

  public static DateTimeFormat getFormat(final PredefinedFormat predefined) {
    return getFormat(predefined.getPattern());
  }

  public String getPattern() {
    return pattern;
  }

  public String format(final Date date) {
    return date == null ? "" : format.format(date);
  }

  public Date parse(final String text) {
    try {
      return format.parse(text);
    } catch (final ParseException e) {
      throw new IllegalArgumentException(
          "Cannot parse \"" + text + "\" with pattern \"" + pattern + "\"", e);
    }
  }

  /** Parses, returning null rather than throwing when the text does not match. */
  public Date parseStrict(final String text) {
    try {
      return format.parse(text);
    } catch (final ParseException e) {
      return null;
    }
  }

  public String format(Date date, TimeZone zone) {
    if (zone == null) return format(date);
    if (date == null) return "";
    SimpleDateFormat zoned = new SimpleDateFormat(pattern);
    int minutes = -zone.getOffset(date);
    int absolute = Math.abs(minutes);
    String id =
        "GMT"
            + (minutes < 0 ? "-" : "+")
            + (absolute / 60 < 10 ? "0" : "")
            + absolute / 60
            + ":"
            + (absolute % 60 < 10 ? "0" : "")
            + absolute % 60;
    zoned.setTimeZone(java.util.TimeZone.getTimeZone(id));
    return zoned.format(date);
  }
}
