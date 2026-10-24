package com.google.gwt.user.client.ui;

public interface HasHorizontalAlignment {
  final class HorizontalAlignmentConstant {
    private final String value;

    private HorizontalAlignmentConstant(String value) {
      this.value = value;
    }

    public String getTextAlignString() {
      return value;
    }
  }

  HorizontalAlignmentConstant ALIGN_LEFT = new HorizontalAlignmentConstant("left");
  HorizontalAlignmentConstant ALIGN_CENTER = new HorizontalAlignmentConstant("center");
  HorizontalAlignmentConstant ALIGN_RIGHT = new HorizontalAlignmentConstant("right");
  HorizontalAlignmentConstant ALIGN_DEFAULT = new HorizontalAlignmentConstant("");
  HorizontalAlignmentConstant ALIGN_JUSTIFY = new HorizontalAlignmentConstant("justify");

  HorizontalAlignmentConstant getHorizontalAlignment();

  void setHorizontalAlignment(HorizontalAlignmentConstant alignment);
}
