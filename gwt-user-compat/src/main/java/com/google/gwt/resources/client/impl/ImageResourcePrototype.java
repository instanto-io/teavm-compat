/* Copyright 2026 Carl Stainton. SPDX-License-Identifier: Apache-2.0 */
package com.google.gwt.resources.client.impl;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;

/**
 * Image metadata compatible with GWT-generated ClientBundle resources, including sprite markers.
 */
public class ImageResourcePrototype implements ImageResource {
  private final String name;
  private final SafeUri uri;
  private final int left, top, width, height;
  private final boolean animated, lossy;

  public ImageResourcePrototype(
      String name,
      SafeUri uri,
      int left,
      int top,
      int width,
      int height,
      boolean animated,
      boolean lossy) {
    this.name = name;
    this.uri = uri;
    this.left = left;
    this.top = top;
    this.width = width;
    this.height = height;
    this.animated = animated;
    this.lossy = lossy;
  }

  public String getName() {
    return name;
  }

  public SafeUri getSafeUri() {
    return uri;
  }

  public String getURL() {
    return uri.asString();
  }

  public int getLeft() {
    return left;
  }

  public int getTop() {
    return top;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean isAnimated() {
    return animated;
  }

  public boolean isLossy() {
    return lossy;
  }

  /** Identifies an image that occupies a rectangle within a bundled sprite. */
  public static class Bundle extends ImageResourcePrototype {
    public Bundle(
        String name,
        SafeUri uri,
        int left,
        int top,
        int width,
        int height,
        boolean animated,
        boolean lossy) {
      super(name, uri, left, top, width, height, animated, lossy);
    }
  }
}
