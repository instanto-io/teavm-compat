package com.google.gwt.geolocation.client;

import com.google.gwt.core.client.Callback;
import org.teavm.jso.*;

/** Delegates location requests to the browser; permission remains under browser control. */
public class Geolocation {
  private final JSObject nativeValue;

  protected Geolocation() {
    nativeValue = current();
  }

  public static class PositionOptions {
    private boolean highAccuracy;
    private int maximumAge, timeout = -1;

    public PositionOptions setHighAccuracyEnabled(boolean value) {
      highAccuracy = value;
      return this;
    }

    public PositionOptions setMaximumAge(int value) {
      maximumAge = value;
      return this;
    }

    public PositionOptions setTimeout(int value) {
      timeout = value;
      return this;
    }
  }

  public static boolean isSupported() {
    return current() != null;
  }

  public static Geolocation getIfSupported() {
    return isSupported() ? new Geolocation() : null;
  }

  public void clearWatch(int id) {
    clear(nativeValue, id);
  }

  public void getCurrentPosition(Callback<Position, PositionError> callback) {
    getCurrentPosition(callback, new PositionOptions());
  }

  public void getCurrentPosition(
      Callback<Position, PositionError> callback, PositionOptions options) {
    request(callback, options, false);
  }

  public int watchPosition(Callback<Position, PositionError> callback) {
    return watchPosition(callback, new PositionOptions());
  }

  public int watchPosition(Callback<Position, PositionError> callback, PositionOptions options) {
    return request(callback, options, true);
  }

  private int request(
      Callback<Position, PositionError> callback, PositionOptions options, boolean watch) {
    return request(
        nativeValue,
        watch,
        value -> callback.onSuccess(new NativePosition(value)),
        value -> callback.onFailure(new PositionError((int) number(value, "code"), message(value))),
        options.highAccuracy,
        options.maximumAge,
        options.timeout);
  }

  @JSFunctor
  private interface Receiver extends JSObject {
    void accept(JSObject value);
  }

  @JSBody(script = "return navigator.geolocation || null;")
  private static native JSObject current();

  @JSBody(
      params = {"geo", "id"},
      script = "geo.clearWatch(id);")
  private static native void clear(JSObject geo, int id);

  @JSBody(
      params = {"geo", "watch", "success", "error", "accuracy", "age", "timeout"},
      script =
          "var options={enableHighAccuracy:accuracy,maximumAge:age};if(timeout>=0)options.timeout=timeout;if(watch)return geo.watchPosition(success,error,options);geo.getCurrentPosition(success,error,options);return 0;")
  private static native int request(
      JSObject geo,
      boolean watch,
      Receiver success,
      Receiver error,
      boolean accuracy,
      int age,
      int timeout);

  @JSBody(
      params = {"value", "key"},
      script = "return value[key];")
  private static native double number(JSObject value, String key);

  @JSBody(
      params = {"value", "key"},
      script = "return value[key] == null;")
  private static native boolean absent(JSObject value, String key);

  @JSBody(params = "value", script = "return value.message;")
  private static native String message(JSObject value);

  @JSBody(params = "value", script = "return value.coords;")
  private static native JSObject coordinates(JSObject value);

  private static final class NativePosition implements Position {
    private final JSObject value;

    NativePosition(JSObject value) {
      this.value = value;
    }

    public double getTimestamp() {
      return number(value, "timestamp");
    }

    public Coordinates getCoordinates() {
      return new NativeCoordinates(coordinates(value));
    }
  }

  private static final class NativeCoordinates implements Position.Coordinates {
    private final JSObject value;

    NativeCoordinates(JSObject value) {
      this.value = value;
    }

    public double getAccuracy() {
      return number(value, "accuracy");
    }

    public double getLatitude() {
      return number(value, "latitude");
    }

    public double getLongitude() {
      return number(value, "longitude");
    }

    public Double getAltitude() {
      return optional("altitude");
    }

    public Double getAltitudeAccuracy() {
      return optional("altitudeAccuracy");
    }

    public Double getHeading() {
      return optional("heading");
    }

    public Double getSpeed() {
      return optional("speed");
    }

    private Double optional(String key) {
      return absent(value, key) ? null : Double.valueOf(number(value, key));
    }
  }
}
