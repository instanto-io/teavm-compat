package com.google.gwt.user.client.ui;

public interface HasVerticalAlignment {
  final class VerticalAlignmentConstant {
    private final String value;

    private VerticalAlignmentConstant(String value) {
      this.value = value;
    }

    public String getVerticalAlignString() {
      return value;
    }
  }

  VerticalAlignmentConstant ALIGN_TOP = new VerticalAlignmentConstant("top");
  VerticalAlignmentConstant ALIGN_MIDDLE = new VerticalAlignmentConstant("middle");
  VerticalAlignmentConstant ALIGN_BOTTOM = new VerticalAlignmentConstant("bottom");

  VerticalAlignmentConstant getVerticalAlignment();

  void setVerticalAlignment(VerticalAlignmentConstant alignment);
}
