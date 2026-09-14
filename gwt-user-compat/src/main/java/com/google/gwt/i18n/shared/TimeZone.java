package com.google.gwt.i18n.shared;

import java.util.Date;

/** GWT time-zone contract; offsets are minutes west of UTC. */
public interface TimeZone {
  int getDaylightAdjustment(Date date);

  String getGMTString(Date date);

  String getID();

  String getISOTimeZoneString(Date date);

  String getLongName(Date date);

  int getOffset(Date date);

  String getRFCTimeZoneString(Date date);

  String getShortName(Date date);

  int getStandardOffset();

  boolean isDaylightTime(Date date);
}
