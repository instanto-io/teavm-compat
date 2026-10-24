package io.instanto.compat;

import com.google.gwt.resources.client.*;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.*;

public final class GeneratedResources {
  public interface Styles extends CssResource {
    String button();

    String external();
  }

  public interface Bundle extends ClientBundle {
    @Source("fixture.txt")
    TextResource text();

    @Source("fixture.css")
    Styles css();

    @Source("pixel.png")
    ImageResource image();

    @Source("fixture.txt")
    DataResource data();
  }

  public interface Templates extends SafeHtmlTemplates {
    @Template("<a href=\"{0}\" title=\"{1}\">{2}{3}</a>")
    SafeHtml link(String uri, String title, String label, SafeHtml trusted);
  }
}
