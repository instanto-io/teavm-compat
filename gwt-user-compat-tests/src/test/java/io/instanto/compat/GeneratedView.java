package io.instanto.compat;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.*;

public class GeneratedView {
  @UiTemplate("GeneratedView.ui.xml")
  public interface ViewBinder extends UiBinder<FlowPanel, GeneratedView> {}

  public interface Style extends CssResource {
    String local();
  }

  @UiField Label label;
  @UiField Style style;
  int constructions;

  @UiFactory
  Label makeLabel() {
    constructions++;
    return new Label();
  }

  public FlowPanel bind() {
    return GWT.<ViewBinder>create(ViewBinder.class).createAndBindUi(this);
  }
}
