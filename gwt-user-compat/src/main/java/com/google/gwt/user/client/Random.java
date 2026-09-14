package com.google.gwt.user.client;

/** GWT's browser random-number helpers. */
public final class Random {
  private static final java.util.Random SOURCE = new java.util.Random();

  private Random() {}

  public static boolean nextBoolean() {
    return SOURCE.nextBoolean();
  }

  public static double nextDouble() {
    return SOURCE.nextDouble();
  }

  public static int nextInt() {
    return SOURCE.nextInt();
  }

  public static int nextInt(int upperBound) {
    return SOURCE.nextInt(upperBound);
  }
}
