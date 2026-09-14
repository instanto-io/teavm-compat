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
package com.google.gwt.core.client;

import org.teavm.jso.*;

/** A Java wrapper retaining the browser Date object and its native date semantics. */
public class JsDate extends JavaScriptObject {
  public static JsDate of(JSObject value) {
    return value == null ? null : new JsDate(value);
  }

  public JsDate(JSObject value) {
    super(value);
  }

  protected JsDate() {
    super(createNative());
  }

  public static JsDate create() {
    return new JsDate(createNative());
  }

  @JSBody(
      params = {},
      script = "return new Date();")
  private static native JSObject createNative();

  public static JsDate create(double milliseconds) {
    return new JsDate(createNative(milliseconds));
  }

  @JSBody(
      params = {"milliseconds"},
      script = "return new Date(milliseconds);")
  private static native JSObject createNative(double milliseconds);

  public static JsDate create(int year, int month) {
    return new JsDate(createNative(year, month));
  }

  @JSBody(
      params = {"year", "month"},
      script = "return new Date(year, month);")
  private static native JSObject createNative(int year, int month);

  public static JsDate create(int year, int month, int dayOfMonth) {
    return new JsDate(createNative(year, month, dayOfMonth));
  }

  @JSBody(
      params = {"year", "month", "dayOfMonth"},
      script = "return new Date(year, month, dayOfMonth);")
  private static native JSObject createNative(int year, int month, int dayOfMonth);

  public static JsDate create(int year, int month, int dayOfMonth, int hours) {
    return new JsDate(createNative(year, month, dayOfMonth, hours));
  }

  @JSBody(
      params = {"year", "month", "dayOfMonth", "hours"},
      script = "return new Date(year, month, dayOfMonth, hours);")
  private static native JSObject createNative(int year, int month, int dayOfMonth, int hours);

  public static JsDate create(int year, int month, int dayOfMonth, int hours, int minutes) {
    return new JsDate(createNative(year, month, dayOfMonth, hours, minutes));
  }

  @JSBody(
      params = {"year", "month", "dayOfMonth", "hours", "minutes"},
      script = "return new Date(year, month, dayOfMonth, hours, minutes);")
  private static native JSObject createNative(
      int year, int month, int dayOfMonth, int hours, int minutes);

  public static JsDate create(
      int year, int month, int dayOfMonth, int hours, int minutes, int seconds) {
    return new JsDate(createNative(year, month, dayOfMonth, hours, minutes, seconds));
  }

  @JSBody(
      params = {"year", "month", "dayOfMonth", "hours", "minutes", "seconds"},
      script = "return new Date(year, month, dayOfMonth, hours, minutes, seconds);")
  private static native JSObject createNative(
      int year, int month, int dayOfMonth, int hours, int minutes, int seconds);

  public static JsDate create(
      int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int millis) {
    return new JsDate(createNative(year, month, dayOfMonth, hours, minutes, seconds, millis));
  }

  @JSBody(
      params = {"year", "month", "dayOfMonth", "hours", "minutes", "seconds", "millis"},
      script = "return new Date(year, month, dayOfMonth, hours, minutes, seconds, millis);")
  private static native JSObject createNative(
      int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int millis);

  public static JsDate create(String dateString) {
    return new JsDate(createNative(dateString));
  }

  @JSBody(
      params = {"dateString"},
      script = "return new Date(dateString);")
  private static native JSObject createNative(String dateString);

  public static double now() {
    return nowNative();
  }

  @JSBody(
      params = {},
      script = "return Date.now();")
  private static native double nowNative();

  public static double parse(String dateString) {
    return parseNative(dateString);
  }

  @JSBody(
      params = {"dateString"},
      script = "return Date.parse(dateString);")
  private static native double parseNative(String dateString);

  public static double UTC(
      int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int millis) {
    return UTCNative(year, month, dayOfMonth, hours, minutes, seconds, millis);
  }

  @JSBody(
      params = {"year", "month", "dayOfMonth", "hours", "minutes", "seconds", "millis"},
      script = "return Date.UTC(year, month, dayOfMonth, hours, minutes, seconds, millis);")
  private static native double UTCNative(
      int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int millis);

  public final int getDate() {
    return getDateNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getDate();")
  private static native int getDateNative(JSObject date);

  public final int getDay() {
    return getDayNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getDay();")
  private static native int getDayNative(JSObject date);

  public final int getFullYear() {
    return getFullYearNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getFullYear();")
  private static native int getFullYearNative(JSObject date);

  public final int getHours() {
    return getHoursNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getHours();")
  private static native int getHoursNative(JSObject date);

  public final int getMilliseconds() {
    return getMillisecondsNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getMilliseconds();")
  private static native int getMillisecondsNative(JSObject date);

  public final int getMinutes() {
    return getMinutesNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getMinutes();")
  private static native int getMinutesNative(JSObject date);

  public final int getMonth() {
    return getMonthNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getMonth();")
  private static native int getMonthNative(JSObject date);

  public final int getSeconds() {
    return getSecondsNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getSeconds();")
  private static native int getSecondsNative(JSObject date);

  public final double getTime() {
    return getTimeNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getTime();")
  private static native double getTimeNative(JSObject date);

  public final int getTimezoneOffset() {
    return getTimezoneOffsetNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getTimezoneOffset();")
  private static native int getTimezoneOffsetNative(JSObject date);

  public final int getUTCDate() {
    return getUTCDateNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCDate();")
  private static native int getUTCDateNative(JSObject date);

  public final int getUTCDay() {
    return getUTCDayNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCDay();")
  private static native int getUTCDayNative(JSObject date);

  public final int getUTCFullYear() {
    return getUTCFullYearNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCFullYear();")
  private static native int getUTCFullYearNative(JSObject date);

  public final int getUTCHours() {
    return getUTCHoursNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCHours();")
  private static native int getUTCHoursNative(JSObject date);

  public final int getUTCMilliseconds() {
    return getUTCMillisecondsNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCMilliseconds();")
  private static native int getUTCMillisecondsNative(JSObject date);

  public final int getUTCMinutes() {
    return getUTCMinutesNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCMinutes();")
  private static native int getUTCMinutesNative(JSObject date);

  public final int getUTCMonth() {
    return getUTCMonthNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCMonth();")
  private static native int getUTCMonthNative(JSObject date);

  public final int getUTCSeconds() {
    return getUTCSecondsNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getUTCSeconds();")
  private static native int getUTCSecondsNative(JSObject date);

  public final int getYear() {
    return getYearNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.getYear();")
  private static native int getYearNative(JSObject date);

  public final double setDate(int dayOfMonth) {
    return setDateNative(unwrap(), dayOfMonth);
  }

  @JSBody(
      params = {"date", "dayOfMonth"},
      script = "return date.setDate(dayOfMonth);")
  private static native double setDateNative(JSObject date, int dayOfMonth);

  public final double setFullYear(int year) {
    return setFullYearNative(unwrap(), year);
  }

  @JSBody(
      params = {"date", "year"},
      script = "return date.setFullYear(year);")
  private static native double setFullYearNative(JSObject date, int year);

  public final double setFullYear(int year, int month) {
    return setFullYearNative(unwrap(), year, month);
  }

  @JSBody(
      params = {"date", "year", "month"},
      script = "return date.setFullYear(year, month);")
  private static native double setFullYearNative(JSObject date, int year, int month);

  public final double setFullYear(int year, int month, int day) {
    return setFullYearNative(unwrap(), year, month, day);
  }

  @JSBody(
      params = {"date", "year", "month", "day"},
      script = "return date.setFullYear(year, month, day);")
  private static native double setFullYearNative(JSObject date, int year, int month, int day);

  public final double setHours(int hours) {
    return setHoursNative(unwrap(), hours);
  }

  @JSBody(
      params = {"date", "hours"},
      script = "return date.setHours(hours);")
  private static native double setHoursNative(JSObject date, int hours);

  public final double setHours(int hours, int mins) {
    return setHoursNative(unwrap(), hours, mins);
  }

  @JSBody(
      params = {"date", "hours", "mins"},
      script = "return date.setHours(hours, mins);")
  private static native double setHoursNative(JSObject date, int hours, int mins);

  public final double setHours(int hours, int mins, int secs) {
    return setHoursNative(unwrap(), hours, mins, secs);
  }

  @JSBody(
      params = {"date", "hours", "mins", "secs"},
      script = "return date.setHours(hours, mins, secs);")
  private static native double setHoursNative(JSObject date, int hours, int mins, int secs);

  public final double setHours(int hours, int mins, int secs, int ms) {
    return setHoursNative(unwrap(), hours, mins, secs, ms);
  }

  @JSBody(
      params = {"date", "hours", "mins", "secs", "ms"},
      script = "return date.setHours(hours, mins, secs, ms);")
  private static native double setHoursNative(JSObject date, int hours, int mins, int secs, int ms);

  public final double setMinutes(int minutes) {
    return setMinutesNative(unwrap(), minutes);
  }

  @JSBody(
      params = {"date", "minutes"},
      script = "return date.setMinutes(minutes);")
  private static native double setMinutesNative(JSObject date, int minutes);

  public final double setMinutes(int minutes, int seconds) {
    return setMinutesNative(unwrap(), minutes, seconds);
  }

  @JSBody(
      params = {"date", "minutes", "seconds"},
      script = "return date.setMinutes(minutes, seconds);")
  private static native double setMinutesNative(JSObject date, int minutes, int seconds);

  public final double setMinutes(int minutes, int seconds, int millis) {
    return setMinutesNative(unwrap(), minutes, seconds, millis);
  }

  @JSBody(
      params = {"date", "minutes", "seconds", "millis"},
      script = "return date.setMinutes(minutes, seconds, millis);")
  private static native double setMinutesNative(
      JSObject date, int minutes, int seconds, int millis);

  public final double setMonth(int month) {
    return setMonthNative(unwrap(), month);
  }

  @JSBody(
      params = {"date", "month"},
      script = "return date.setMonth(month);")
  private static native double setMonthNative(JSObject date, int month);

  public final double setMonth(int month, int dayOfMonth) {
    return setMonthNative(unwrap(), month, dayOfMonth);
  }

  @JSBody(
      params = {"date", "month", "dayOfMonth"},
      script = "return date.setMonth(month, dayOfMonth);")
  private static native double setMonthNative(JSObject date, int month, int dayOfMonth);

  public final double setSeconds(int seconds) {
    return setSecondsNative(unwrap(), seconds);
  }

  @JSBody(
      params = {"date", "seconds"},
      script = "return date.setSeconds(seconds);")
  private static native double setSecondsNative(JSObject date, int seconds);

  public final double setSeconds(int seconds, int millis) {
    return setSecondsNative(unwrap(), seconds, millis);
  }

  @JSBody(
      params = {"date", "seconds", "millis"},
      script = "return date.setSeconds(seconds, millis);")
  private static native double setSecondsNative(JSObject date, int seconds, int millis);

  public final double setTime(double milliseconds) {
    return setTimeNative(unwrap(), milliseconds);
  }

  @JSBody(
      params = {"date", "milliseconds"},
      script = "return date.setTime(milliseconds);")
  private static native double setTimeNative(JSObject date, double milliseconds);

  public final double setUTCDate(int dayOfMonth) {
    return setUTCDateNative(unwrap(), dayOfMonth);
  }

  @JSBody(
      params = {"date", "dayOfMonth"},
      script = "return date.setUTCDate(dayOfMonth);")
  private static native double setUTCDateNative(JSObject date, int dayOfMonth);

  public final double setUTCFullYear(int year) {
    return setUTCFullYearNative(unwrap(), year);
  }

  @JSBody(
      params = {"date", "year"},
      script = "return date.setUTCFullYear(year);")
  private static native double setUTCFullYearNative(JSObject date, int year);

  public final double setUTCFullYear(int year, int month) {
    return setUTCFullYearNative(unwrap(), year, month);
  }

  @JSBody(
      params = {"date", "year", "month"},
      script = "return date.setUTCFullYear(year, month);")
  private static native double setUTCFullYearNative(JSObject date, int year, int month);

  public final double setUTCFullYear(int year, int month, int day) {
    return setUTCFullYearNative(unwrap(), year, month, day);
  }

  @JSBody(
      params = {"date", "year", "month", "day"},
      script = "return date.setUTCFullYear(year, month, day);")
  private static native double setUTCFullYearNative(JSObject date, int year, int month, int day);

  public final double setUTCHours(int hours) {
    return setUTCHoursNative(unwrap(), hours);
  }

  @JSBody(
      params = {"date", "hours"},
      script = "return date.setUTCHours(hours);")
  private static native double setUTCHoursNative(JSObject date, int hours);

  public final double setUTCHours(int hours, int mins) {
    return setUTCHoursNative(unwrap(), hours, mins);
  }

  @JSBody(
      params = {"date", "hours", "mins"},
      script = "return date.setUTCHours(hours, mins);")
  private static native double setUTCHoursNative(JSObject date, int hours, int mins);

  public final double setUTCHours(int hours, int mins, int secs) {
    return setUTCHoursNative(unwrap(), hours, mins, secs);
  }

  @JSBody(
      params = {"date", "hours", "mins", "secs"},
      script = "return date.setUTCHours(hours, mins, secs);")
  private static native double setUTCHoursNative(JSObject date, int hours, int mins, int secs);

  public final double setUTCHours(int hours, int mins, int secs, int ms) {
    return setUTCHoursNative(unwrap(), hours, mins, secs, ms);
  }

  @JSBody(
      params = {"date", "hours", "mins", "secs", "ms"},
      script = "return date.setUTCHours(hours, mins, secs, ms);")
  private static native double setUTCHoursNative(
      JSObject date, int hours, int mins, int secs, int ms);

  public final double setUTCMinutes(int minutes) {
    return setUTCMinutesNative(unwrap(), minutes);
  }

  @JSBody(
      params = {"date", "minutes"},
      script = "return date.setUTCMinutes(minutes);")
  private static native double setUTCMinutesNative(JSObject date, int minutes);

  public final double setUTCMinutes(int minutes, int seconds) {
    return setUTCMinutesNative(unwrap(), minutes, seconds);
  }

  @JSBody(
      params = {"date", "minutes", "seconds"},
      script = "return date.setUTCMinutes(minutes, seconds);")
  private static native double setUTCMinutesNative(JSObject date, int minutes, int seconds);

  public final double setUTCMinutes(int minutes, int seconds, int millis) {
    return setUTCMinutesNative(unwrap(), minutes, seconds, millis);
  }

  @JSBody(
      params = {"date", "minutes", "seconds", "millis"},
      script = "return date.setUTCMinutes(minutes, seconds, millis);")
  private static native double setUTCMinutesNative(
      JSObject date, int minutes, int seconds, int millis);

  public final double setUTCMonth(int month) {
    return setUTCMonthNative(unwrap(), month);
  }

  @JSBody(
      params = {"date", "month"},
      script = "return date.setUTCMonth(month);")
  private static native double setUTCMonthNative(JSObject date, int month);

  public final double setUTCMonth(int month, int dayOfMonth) {
    return setUTCMonthNative(unwrap(), month, dayOfMonth);
  }

  @JSBody(
      params = {"date", "month", "dayOfMonth"},
      script = "return date.setUTCMonth(month, dayOfMonth);")
  private static native double setUTCMonthNative(JSObject date, int month, int dayOfMonth);

  public final double setUTCSeconds(int seconds) {
    return setUTCSecondsNative(unwrap(), seconds);
  }

  @JSBody(
      params = {"date", "seconds"},
      script = "return date.setUTCSeconds(seconds);")
  private static native double setUTCSecondsNative(JSObject date, int seconds);

  public final double setUTCSeconds(int seconds, int millis) {
    return setUTCSecondsNative(unwrap(), seconds, millis);
  }

  @JSBody(
      params = {"date", "seconds", "millis"},
      script = "return date.setUTCSeconds(seconds, millis);")
  private static native double setUTCSecondsNative(JSObject date, int seconds, int millis);

  public final double setYear(int year) {
    return setYearNative(unwrap(), year);
  }

  @JSBody(
      params = {"date", "year"},
      script = "return date.setYear(year);")
  private static native double setYearNative(JSObject date, int year);

  public final String toDateString() {
    return toDateStringNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.toDateString();")
  private static native String toDateStringNative(JSObject date);

  public final String toGMTString() {
    return toGMTStringNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.toGMTString();")
  private static native String toGMTStringNative(JSObject date);

  public final String toLocaleDateString() {
    return toLocaleDateStringNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.toLocaleDateString();")
  private static native String toLocaleDateStringNative(JSObject date);

  public final String toLocaleString() {
    return toLocaleStringNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.toLocaleString();")
  private static native String toLocaleStringNative(JSObject date);

  public final String toLocaleTimeString() {
    return toLocaleTimeStringNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.toLocaleTimeString();")
  private static native String toLocaleTimeStringNative(JSObject date);

  public final String toTimeString() {
    return toTimeStringNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.toTimeString();")
  private static native String toTimeStringNative(JSObject date);

  public final String toUTCString() {
    return toUTCStringNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.toUTCString();")
  private static native String toUTCStringNative(JSObject date);

  public final double valueOf() {
    return valueOfNative(unwrap());
  }

  @JSBody(
      params = {"date"},
      script = "return date.valueOf();")
  private static native double valueOfNative(JSObject date);
}
