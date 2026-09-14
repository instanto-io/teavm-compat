package example.uibinder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class GreetingView extends Composite {
  interface Binder extends UiBinder<Widget, GreetingView> {}

  private static final Binder BINDER = GWT.create(Binder.class);

  @UiField Button greet;
  @UiField Label result;

  public GreetingView() {
    initWidget(BINDER.createAndBindUi(this));
  }

  @UiHandler("greet")
  void greet(ClickEvent event) {
    result.setText("Hello from UiBinder");
  }
}
