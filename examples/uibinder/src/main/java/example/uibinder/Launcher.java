package example.uibinder;

import com.google.gwt.user.client.ui.RootPanel;

public final class Launcher {
  public static void main(String[] args) {
    RootPanel.get().add(new GreetingView());
  }
}
